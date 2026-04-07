package com.gridclash.app.ui.multiplayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.core.model.NetworkStatus
import com.gridclash.app.data.AppPreferencesRepository
import com.gridclash.app.network.MessageType
import com.gridclash.app.network.NetworkMessage
import com.gridclash.app.network.NetworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MultiplayerUiState(
    val localIp: String = "",
    val localPseudo: String = "Joueur",
    val remotePseudo: String = "En attente...",
    val isHosting: Boolean = false,
    val isConnecting: Boolean = false,
    val clientConnected: Boolean = false,
    val inputIp: String = "",
    val error: String? = null,
    val gameStarted: Boolean = false,
    val role: String = "",
    val networkStatus: NetworkStatus = NetworkStatus.IDLE,
    val gridSize: GridSize = GridSize.THREE,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val winLength: Int = 3
)

class MultiplayerViewModel(
    private val networkRepository: NetworkRepository,
    private val preferencesRepository: AppPreferencesRepository,
    context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiplayerUiState(localIp = NetworkRepository.getLocalIp(context)))
    val uiState: StateFlow<MultiplayerUiState> = _uiState

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.settings.first()
            _uiState.update {
                it.copy(
                    localPseudo = prefs.lastLocalPseudo,
                    inputIp = prefs.lastHostIp,
                    gridSize = prefs.lastGridSize,
                    difficulty = prefs.lastDifficulty,
                    winLength = recommendedWinLength(prefs.lastGridSize)
                )
            }
        }
    }

    fun updateInputIp(ip: String) = _uiState.update { it.copy(inputIp = ip) }
    fun updatePseudo(name: String) = _uiState.update { it.copy(localPseudo = name.take(20)) }
    fun updateGridSize(gridSize: GridSize) = _uiState.update { it.copy(gridSize = gridSize, winLength = recommendedWinLength(gridSize)) }
    fun updateDifficulty(difficulty: Difficulty) = _uiState.update { it.copy(difficulty = difficulty) }

    fun startHosting() {
        val state = _uiState.value
        val pseudo = state.localPseudo.trim()
        if (pseudo.length < 2) {
            _uiState.update { it.copy(error = "Pseudo trop court (min. 2 caractères)") }
            return
        }
        val server = networkRepository.createServer()
        networkRepository.lobbyConfig = NetworkRepository.LobbyConfig(
            hostName = pseudo,
            gridSize = state.gridSize,
            difficulty = state.difficulty,
            winLength = state.winLength
        )
        _uiState.update { it.copy(isHosting = true, error = null, role = "HOST", networkStatus = NetworkStatus.WAITING) }

        viewModelScope.launch {
            preferencesRepository.setLastPseudo(pseudo)
            preferencesRepository.setLastGridSize(state.gridSize)
            preferencesRepository.setLastDifficulty(state.difficulty)
            server.startAndWait().onSuccess {
                observeServerMessages(server)
            }.onFailure { e ->
                _uiState.update { it.copy(error = "Erreur serveur : ${e.message}", isHosting = false, networkStatus = NetworkStatus.ERROR) }
                networkRepository.stopServer()
            }
        }
    }

    fun joinGame() {
        val state = _uiState.value
        val pseudo = state.localPseudo.trim()
        val ip = state.inputIp.trim()
        when {
            pseudo.length < 2 -> _uiState.update { it.copy(error = "Pseudo trop court (min. 2 caractères)") }
            ip.isBlank() -> _uiState.update { it.copy(error = "Saisis l'IP de l'hôte") }
            else -> connectToHost(ip, pseudo)
        }
    }

    private fun connectToHost(ip: String, pseudo: String) {
        val client = networkRepository.createClient()
        _uiState.update { it.copy(isConnecting = true, error = null, role = "CLIENT", networkStatus = NetworkStatus.WAITING) }

        viewModelScope.launch {
            preferencesRepository.setLastPseudo(pseudo)
            preferencesRepository.setLastHostIp(ip)
            client.connect(ip).onSuccess {
                networkRepository.sendAsClient(NetworkMessage(type = MessageType.PLAYER_JOIN, playerName = pseudo))
                observeClientMessages(client)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isConnecting = false, error = "Impossible de joindre $ip : ${e.message}", networkStatus = NetworkStatus.ERROR)
                }
                networkRepository.disconnectClient()
            }
        }
    }

    private fun observeServerMessages(server: com.gridclash.app.network.GameServer) {
        viewModelScope.launch {
            server.incoming.collect { msg ->
                when (msg.type) {
                    MessageType.PLAYER_JOIN -> {
                        val remote = msg.playerName ?: "Client"
                        networkRepository.lobbyConfig = networkRepository.lobbyConfig.copy(clientName = remote)
                        _uiState.update { it.copy(clientConnected = true, remotePseudo = remote, networkStatus = NetworkStatus.CONNECTED) }
                        networkRepository.sendAsHost(
                            NetworkMessage(
                                type = MessageType.GAME_START,
                                hostName = networkRepository.lobbyConfig.hostName,
                                clientName = remote,
                                hostSymbol = "X",
                                clientSymbol = "O",
                                gridSize = networkRepository.lobbyConfig.gridSize.size,
                                winLength = networkRepository.lobbyConfig.winLength,
                                difficulty = networkRepository.lobbyConfig.difficulty.name
                            )
                        )
                        _uiState.update { it.copy(gameStarted = true) }
                    }
                    MessageType.DISCONNECT -> _uiState.update { it.copy(clientConnected = false, error = "Le client s'est déconnecté", networkStatus = NetworkStatus.DISCONNECTED) }
                    else -> Unit
                }
            }
        }
    }

    private fun observeClientMessages(client: com.gridclash.app.network.GameClient) {
        viewModelScope.launch {
            client.incoming.collect { msg ->
                when (msg.type) {
                    MessageType.GAME_START -> {
                        val size = when (msg.gridSize) {
                            4 -> GridSize.FOUR
                            5 -> GridSize.FIVE
                            else -> GridSize.THREE
                        }
                        networkRepository.lobbyConfig = NetworkRepository.LobbyConfig(
                            hostName = msg.hostName ?: "Hôte",
                            clientName = msg.clientName ?: _uiState.value.localPseudo,
                            gridSize = size,
                            difficulty = runCatching { Difficulty.valueOf(msg.difficulty ?: Difficulty.MEDIUM.name) }.getOrDefault(Difficulty.MEDIUM),
                            winLength = msg.winLength ?: recommendedWinLength(size)
                        )
                        _uiState.update {
                            it.copy(
                                isConnecting = false,
                                remotePseudo = msg.hostName ?: "Hôte",
                                gameStarted = true,
                                networkStatus = NetworkStatus.CONNECTED
                            )
                        }
                    }
                    MessageType.DISCONNECT -> _uiState.update { it.copy(error = "L'hôte s'est déconnecté", networkStatus = NetworkStatus.DISCONNECTED) }
                    else -> Unit
                }
            }
        }
    }

    fun stopHosting() {
        networkRepository.stopServer()
        _uiState.update { it.copy(isHosting = false, clientConnected = false, role = "", networkStatus = NetworkStatus.IDLE) }
    }

    private fun recommendedWinLength(gridSize: GridSize): Int = when (gridSize) {
        GridSize.THREE -> 3
        GridSize.FOUR -> 3
        GridSize.FIVE -> 4
    }
}
