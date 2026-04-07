package com.gridclash.app.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.flow.SharedFlow
import java.net.Inet4Address
import java.net.NetworkInterface

private const val TAG = "NetworkRepository"

/**
 * Singleton qui encapsule GameServer et GameClient.
 * Partagé entre MultiplayerViewModel et GameViewModel.
 */
class NetworkRepository {

    private var server: GameServer? = null
    private var client: GameClient? = null

    // ─── Accès aux flux de messages ──────────────────────────────────────────

    val serverIncoming: SharedFlow<NetworkMessage>?
        get() = server?.incoming

    val clientIncoming: SharedFlow<NetworkMessage>?
        get() = client?.incoming

    val isHosting: Boolean get() = server != null
    val isClient: Boolean  get() = client != null

    // ─── Hôte ─────────────────────────────────────────────────────────────────

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

    // ─── Client ───────────────────────────────────────────────────────────────

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

    // ─── Envoi générique (selon le rôle) ─────────────────────────────────────

    suspend fun send(message: NetworkMessage) {
        when {
            isHosting -> sendAsHost(message)
            isClient  -> sendAsClient(message)
        }
    }

    // ─── Nettoyage ────────────────────────────────────────────────────────────

    fun clear() {
        stopServer()
        disconnectClient()
    }

    // ─── Utilitaire IP locale ─────────────────────────────────────────────────

    companion object {
        fun getLocalIp(context: Context): String {
            // Méthode 1 : WifiManager (rapide)
            runCatching {
                val wm = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                val ip = wm.connectionInfo.ipAddress
                if (ip != 0) {
                    return String.format(
                        "%d.%d.%d.%d",
                        ip and 0xff, ip shr 8 and 0xff,
                        ip shr 16 and 0xff, ip shr 24 and 0xff
                    )
                }
            }
            // Méthode 2 : NetworkInterface (fonctionne aussi en hotspot)
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
