package com.gridclash.app.ui.multiplayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridclash.app.network.MessageType
import com.gridclash.app.network.NetworkMessage
import com.gridclash.app.network.NetworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MultiplayerUiState(
    val localIp: String         = "",
    val isHosting: Boolean      = false,
    val isConnecting: Boolean   = false,
    val clientConnected: Boolean = false,
    val inputIp: String         = "",
    val error: String?          = null,
    val gameStarted: Boolean    = false,
    val role: String            = ""   // "HOST" ou "CLIENT"
)

class MultiplayerViewModel(
    private val networkRepository: NetworkRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiplayerUiState())
    val uiState: StateFlow<MultiplayerUiState> = _uiState

    init {
        _uiState.update { it.copy(localIp = NetworkRepository.getLocalIp(context)) }
    }

    // ── Hôte : démarrer le serveur ────────────────────────────────────────────

    fun startHosting() {
        val server = networkRepository.createServer()
        _uiState.update { it.copy(isHosting = true, error = null, role = "HOST") }

        viewModelScope.launch {
            server.startAndWait()
                .onSuccess {
                    // Le client s'est connecté — écouter son premier message PLAYER_JOIN
                    observeServerMessages(server)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Erreur serveur : ${e.message}", isHosting = false) }
                    networkRepository.stopServer()
                }
        }
    }

    private fun observeServerMessages(server: com.gridclash.app.network.GameServer) {
        viewModelScope.launch {
            server.incoming.collect { msg ->
                when (msg.type) {
                    MessageType.PLAYER_JOIN -> {
                        _uiState.update { it.copy(clientConnected = true) }
                        // Envoyer GAME_START au client
                        networkRepository.sendAsHost(
                            NetworkMessage(
                                type         = MessageType.GAME_START,
                                hostSymbol   = "X",
                                clientSymbol = "O"
                            )
                        )
                        _uiState.update { it.copy(gameStarted = true) }
                    }
                    MessageType.DISCONNECT -> {
                        _uiState.update { it.copy(clientConnected = false, error = "Le client s'est déconnecté") }
                    }
                    else -> Unit
                }
            }
        }
    }

    // ── Client : rejoindre un hôte ────────────────────────────────────────────

    fun updateInputIp(ip: String) = _uiState.update { it.copy(inputIp = ip) }

    fun joinGame() {
        val ip = _uiState.value.inputIp.trim()
        if (ip.isEmpty()) { _uiState.update { it.copy(error = "Saisis l'IP de l'hôte") }; return }

        val client = networkRepository.createClient()
        _uiState.update { it.copy(isConnecting = true, error = null, role = "CLIENT") }

        viewModelScope.launch {
            client.connect(ip)
                .onSuccess {
                    // Connexion TCP OK — envoyer PLAYER_JOIN
                    networkRepository.sendAsClient(
                        NetworkMessage(type = MessageType.PLAYER_JOIN, playerName = "Joueur")
                    )
                    observeClientMessages(client)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isConnecting = false, error = "Impossible de joindre $ip : ${e.message}")
                    }
                    networkRepository.disconnectClient()
                }
        }
    }

    private fun observeClientMessages(client: com.gridclash.app.network.GameClient) {
        viewModelScope.launch {
            client.incoming.collect { msg ->
                when (msg.type) {
                    MessageType.GAME_START -> {
                        _uiState.update { it.copy(isConnecting = false, gameStarted = true) }
                    }
                    MessageType.DISCONNECT -> {
                        _uiState.update { it.copy(error = "L'hôte s'est déconnecté") }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun stopHosting() {
        networkRepository.stopServer()
        _uiState.update { it.copy(isHosting = false, clientConnected = false, role = "") }
    }

    override fun onCleared() {
        super.onCleared()
        // Ne pas stopper le réseau ici — GameViewModel en a besoin
    }
}
