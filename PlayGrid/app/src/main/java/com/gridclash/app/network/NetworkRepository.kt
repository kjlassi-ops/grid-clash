package com.gridclash.app.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize
import java.net.Inet4Address
import java.net.NetworkInterface

private const val TAG = "NetworkRepository"

class NetworkRepository {

    data class LobbyConfig(
        val hostName: String = "Hôte",
        val clientName: String = "Client",
        val gridSize: GridSize = GridSize.THREE,
        val winLength: Int = 3,
        val difficulty: Difficulty = Difficulty.MEDIUM
    )

    var lobbyConfig: LobbyConfig = LobbyConfig()

    private var server: GameServer? = null
    private var client: GameClient? = null

    val serverIncoming get() = server?.incoming
    val clientIncoming get() = client?.incoming

    val isHosting: Boolean get() = server != null
    val isClient: Boolean get() = client != null

    fun createServer(): GameServer {
        server?.stop()
        return GameServer().also { server = it }
    }

    suspend fun sendAsHost(message: NetworkMessage) {
        server?.send(message) ?: Log.w(TAG, "Pas de serveur actif")
    }

    fun stopServer() {
        server?.stop()
        server = null
    }

    fun createClient(): GameClient {
        client?.disconnect()
        return GameClient().also { client = it }
    }

    suspend fun sendAsClient(message: NetworkMessage) {
        client?.send(message) ?: Log.w(TAG, "Pas de client actif")
    }

    fun disconnectClient() {
        client?.disconnect()
        client = null
    }

    suspend fun send(message: NetworkMessage) {
        when {
            isHosting -> sendAsHost(message)
            isClient -> sendAsClient(message)
        }
    }

    fun clear() {
        stopServer()
        disconnectClient()
        lobbyConfig = LobbyConfig()
    }

    companion object {
        fun getLocalIp(context: Context): String {
            runCatching {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val ip = wm.connectionInfo.ipAddress
                if (ip != 0) {
                    return String.format(
                        "%d.%d.%d.%d",
                        ip and 0xff, ip shr 8 and 0xff,
                        ip shr 16 and 0xff, ip shr 24 and 0xff
                    )
                }
            }
            runCatching {
                NetworkInterface.getNetworkInterfaces()?.toList()
                    ?.flatMap { it.inetAddresses.toList() }
                    ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                    ?.let { return it.hostAddress ?: "Introuvable" }
            }
            return "Introuvable"
        }
    }
}
