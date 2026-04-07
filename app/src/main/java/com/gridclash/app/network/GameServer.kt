package com.gridclash.app.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

private const val TAG = "GameServer"
const val GAME_PORT = 5555

class GameServer {

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null

    private val _incoming = MutableSharedFlow<NetworkMessage>(extraBufferCapacity = 32)
    val incoming: SharedFlow<NetworkMessage> = _incoming

    var isClientConnected: Boolean = false
        private set

    /**
     * Lance le serveur TCP et attend un client.
     * À appeler dans un coroutine scope (Dispatchers.IO).
     */
    suspend fun startAndWait(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            serverSocket = ServerSocket(GAME_PORT)
            Log.d(TAG, "Serveur démarré sur port $GAME_PORT")

            val socket = serverSocket!!.accept() // bloquant jusqu'à connexion client
            clientSocket = socket
            isClientConnected = true
            writer = PrintWriter(socket.getOutputStream(), true)

            Log.d(TAG, "Client connecté : ${socket.inetAddress.hostAddress}")

            // Lecture en boucle des messages entrants
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { json ->
                    runCatching {
                        val msg = Json.decodeFromString<NetworkMessage>(json)
                        _incoming.emit(msg)
                    }.onFailure { Log.e(TAG, "Parse error: $it") }
                }
            }

            Log.d(TAG, "Connexion client fermée")
            isClientConnected = false
        }
    }

    /** Envoie un message au client connecté. */
    suspend fun send(message: NetworkMessage) = withContext(Dispatchers.IO) {
        runCatching {
            writer?.println(Json.encodeToString(message))
        }.onFailure { Log.e(TAG, "Erreur envoi: $it") }
    }

    fun stop() {
        runCatching { writer?.close() }
        runCatching { clientSocket?.close() }
        runCatching { serverSocket?.close() }
        isClientConnected = false
        Log.d(TAG, "Serveur arrêté")
    }
}
