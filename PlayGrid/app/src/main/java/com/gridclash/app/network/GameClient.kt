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
import java.net.Socket

private const val TAG = "GameClient"
private const val TIMEOUT_MS = 10_000

class GameClient {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    private val _incoming = MutableSharedFlow<NetworkMessage>(extraBufferCapacity = 32)
    val incoming: SharedFlow<NetworkMessage> = _incoming

    var isConnected: Boolean = false
        private set

    /**
     * Se connecte à l'hôte et démarre la lecture des messages.
     * À appeler dans un coroutine scope (Dispatchers.IO).
     */
    suspend fun connect(hostIp: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val sock = Socket()
            sock.connect(java.net.InetSocketAddress(hostIp, GAME_PORT), TIMEOUT_MS)
            socket = sock
            isConnected = true
            writer = PrintWriter(sock.getOutputStream(), true)

            Log.d(TAG, "Connecté à $hostIp:$GAME_PORT")

            val reader = BufferedReader(InputStreamReader(sock.getInputStream()))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { json ->
                    runCatching {
                        val msg = Json.decodeFromString<NetworkMessage>(json)
                        _incoming.emit(msg)
                    }.onFailure { Log.e(TAG, "Parse error: $it") }
                }
            }

            Log.d(TAG, "Connexion hôte fermée")
            isConnected = false
        }
    }

    /** Envoie un message à l'hôte. */
    suspend fun send(message: NetworkMessage) = withContext(Dispatchers.IO) {
        runCatching {
            writer?.println(Json.encodeToString(message))
        }.onFailure { Log.e(TAG, "Erreur envoi: $it") }
    }

    fun disconnect() {
        runCatching { writer?.close() }
        runCatching { socket?.close() }
        isConnected = false
        Log.d(TAG, "Client déconnecté")
    }
}
