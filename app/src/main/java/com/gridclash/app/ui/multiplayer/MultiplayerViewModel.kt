package com.gridclash.app.ui.multiplayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.network.MessageType
import com.gridclash.app.network.NetworkMessage
import com.gridclash.app.network.NetworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MultiplayerUiState(
    val localIp: String           = "",
    val isHosting: Boolean        = false,
    val isConnecting: Boolean     = false,
    val clientConnected: Boolean  = false,
    val inputIp: String           = "",
    val localName: String         = "Joueur",
    val opponentName: String      = "",
    val selectedGridSize: GridSize = GridSize.SMALL,
    val error: String?            = null,
    val gameStarted: Boolean      = false,
    val role: String              = ""     // "HOST" ou "CLIENT"
)

class MultiplayerViewModel(
    private val networkRepository: NetworkRepository,
    private val context: Context,
    initialName: String = "Joueur",
    initialGridSize: GridSize = GridSize.SMALL
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MultiplayerUiState(
            localIp          = NetworkRepository.getLocalIp(context),
            localName        = initialName,
            selectedGridSize = initialGridSize
        )
    )
    val uiState: StateFlow<MultiplayerUiState> = _uiState

    // ── Mise à jour champs ────────────────────────────────────────────────────

    fun updateInputIp(ip: String)     = _uiState.update { it.copy(inputIp = ip) }
    fun updateLocalName(name: String) = _uiState.update { it.copy(localName = name) }
    fun updateGridSize(gs: GridSize)  = _uiState.update { it.copy(selectedGridSize = gs) }

    // ── Hôte : démarrer le serveur ────────────────────────────────────────────

    fun startHosting() {
        val server = networkRepository.createServer()
        _uiState.update { it.copy(isHosting = true, error = null, role = "HOST") }

        viewModelScope.launch {
            server.startAndWait()
                .onSuccess { observeServerMessages(server) }
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
                        val clientName = msg.playerName?.take(20)?.ifBlank { "Client" } ?: "Client"
                        _uiState.update { it.copy(clientConnected = true, opponentName = clientName) }
                        // Envoyer GAME_START avec taille de grille + nom hôte
                        networkRepository.sendAsHost(
                            NetworkMessage(
                                type         = MessageType.GAME_START,
                                hostSymbol   = "X",
                                clientSymbol = "O",
                                gridSize     = _uiState.value.selectedGridSize.name,
                                hostName     = _uiState.value.localName
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

    fun joinGame() {
        val ip = _uiState.value.inputIp.trim()
        if (ip.isEmpty()) { _uiState.update { it.copy(error = "Saisis l'IP de l'hôte") }; return }

        val client = networkRepository.createClient()
        _uiState.update { it.copy(isConnecting = true, error = null, role = "CLIENT") }

        viewModelScope.launch {
            client.connect(ip)
                .onSuccess {
                    networkRepository.sendAsClient(
                        NetworkMessage(
                            type       = MessageType.PLAYER_JOIN,
                            playerName = _uiState.value.localName
                        )
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
                        val gs = msg.gridSize?.let { runCatching { GridSize.valueOf(it) }.getOrNull() }
                            ?: GridSize.SMALL
                        val hostName = msg.hostName?.take(20)?.ifBlank { "Hôte" } ?: "Hôte"
                        _uiState.update {
                            it.copy(
                                isConnecting     = false,
                                selectedGridSize = gs,
                                opponentName     = hostName,
                                gameStarted      = true
                            )
                        }
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
