package com.gridclash.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridclash.app.audio.GameAudioManager
import com.gridclash.app.core.model.*
import com.gridclash.app.game.ai.BotFactory
import com.gridclash.app.game.engine.GameEngine
import com.gridclash.app.network.MessageType
import com.gridclash.app.network.NetworkMessage
import com.gridclash.app.network.NetworkRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val config: GameConfig,
    private val networkRepository: NetworkRepository,
    private val audioManager: GameAudioManager
) : ViewModel() {

    private val engine = GameEngine()
    private val bot = BotFactory.create(config.difficulty)

    private val localSymbol: PlayerSymbol = when (config.mode) {
        GameMode.SOLO, GameMode.MULTI_HOST -> PlayerSymbol.X
        GameMode.MULTI_CLIENT -> PlayerSymbol.O
    }

    private val _uiState = MutableStateFlow(
        GameUiState(
            mode = config.mode,
            localSymbol = localSymbol,
            localPlayerName = config.localPlayerName,
            opponentName = config.remotePlayerName,
            gridSize = config.gridSize,
            winLength = config.winLength,
            board = List(config.gridSize.size * config.gridSize.size) { CellState.EMPTY },
            isWaitingForOpponent = config.mode != GameMode.SOLO,
            networkStatus = if (config.mode == GameMode.SOLO) NetworkStatus.IDLE else NetworkStatus.CONNECTED
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState

    init { if (config.mode != GameMode.SOLO) observeNetwork() }

    fun onCellClick(index: Int) {
        val state = _uiState.value
        if (!state.isMyTurn || state.isGameOver || state.isThinking) return
        if (state.board[index] != CellState.EMPTY) return

        playMove(index)
        audioManager.onCellPlayed()
        if (config.mode == GameMode.SOLO && !_uiState.value.isGameOver) scheduleBotMove()
    }

    private fun playMove(index: Int) {
        val newState = engine.applyMove(_uiState.value, index) ?: return
        _uiState.value = newState
        handleGameEndAudio(newState)

        if (config.mode != GameMode.SOLO) {
            viewModelScope.launch {
                networkRepository.send(NetworkMessage(type = MessageType.PLAY_MOVE, cellIndex = index))
            }
        }
    }

    private fun handleGameEndAudio(state: GameUiState) {
        when (val result = state.result) {
            is GameResult.Winner -> if (result.symbol == state.localSymbol) audioManager.onWin() else audioManager.onLose()
            GameResult.Draw -> audioManager.onDraw()
            else -> Unit
        }
    }

    private fun scheduleBotMove() {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true) }
            delay(450L)
            val state = _uiState.value
            if (!state.isGameOver) {
                val botSymbol = localSymbol.opponent()
                val move = bot.getMove(state.board, botSymbol, state.gridSize.size, state.winLength)
                engine.applyMove(state, move)?.let { newState ->
                    _uiState.value = newState.copy(isThinking = false)
                    handleGameEndAudio(_uiState.value)
                }
            } else {
                _uiState.update { it.copy(isThinking = false) }
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            val incoming = when (config.mode) {
                GameMode.MULTI_HOST -> networkRepository.serverIncoming
                GameMode.MULTI_CLIENT -> networkRepository.clientIncoming
                else -> null
            } ?: return@launch

            incoming.collect { msg ->
                when (msg.type) {
                    MessageType.PLAY_MOVE -> msg.cellIndex?.let { idx ->
                        val newState = engine.applyMove(_uiState.value, idx)
                        if (newState != null) {
                            _uiState.value = newState
                            audioManager.onCellPlayed()
                            handleGameEndAudio(newState)
                        }
                    }
                    MessageType.REMATCH -> resetBoard()
                    MessageType.DISCONNECT -> _uiState.update {
                        it.copy(connectionError = "Adversaire déconnecté", networkStatus = NetworkStatus.DISCONNECTED)
                    }
                    else -> Unit
                }
            }
        }
    }

    fun rematch() {
        if (config.mode != GameMode.SOLO) {
            viewModelScope.launch { networkRepository.send(NetworkMessage(type = MessageType.REMATCH)) }
        }
        resetBoard()
    }

    private fun resetBoard() {
        _uiState.value = engine.resetBoard(_uiState.value)
    }
}
