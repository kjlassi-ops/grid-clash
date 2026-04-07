package com.gridclash.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridclash.app.audio.AudioManager
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
    private val mode: GameMode,
    private val difficulty: Difficulty,
    private val gridSize: GridSize,
    private val localName: String,
    private val opponentName: String,
    private val networkRepository: NetworkRepository,
    private val audioManager: AudioManager
) : ViewModel() {

    private val engine = GameEngine()
    private val bot    = BotFactory.create(difficulty, gridSize)

    private val localSymbol: PlayerSymbol = when (mode) {
        GameMode.SOLO, GameMode.MULTI_HOST -> PlayerSymbol.X
        GameMode.MULTI_CLIENT              -> PlayerSymbol.O
    }

    private val _uiState = MutableStateFlow(
        GameUiState(
            gridSize         = gridSize,
            board            = List(gridSize.size * gridSize.size) { CellState.EMPTY },
            mode             = mode,
            localSymbol      = localSymbol,
            localPlayerName  = localName.ifBlank { "Toi" },
            opponentName     = opponentName.ifBlank {
                when (mode) {
                    GameMode.SOLO         -> "Bot (${difficulty.label()})"
                    GameMode.MULTI_HOST   -> "Adversaire"
                    GameMode.MULTI_CLIENT -> "Hôte"
                }
            }
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState

    init {
        if (mode != GameMode.SOLO) observeNetwork()
    }

    // ─── Coup local ───────────────────────────────────────────────────────────

    fun onCellClick(index: Int) {
        val state = _uiState.value
        if (!state.isMyTurn || state.isGameOver || state.isThinking) return
        if (state.board[index] != CellState.EMPTY) return

        audioManager.playClick()
        audioManager.vibrate(20L)
        playMove(index)

        if (mode == GameMode.SOLO && !_uiState.value.isGameOver) {
            scheduleBotMove()
        }
    }

    private fun playMove(index: Int) {
        val newState = engine.applyMove(_uiState.value, index) ?: return
        _uiState.value = newState

        // Sons de fin
        when (newState.result) {
            is GameResult.Winner -> {
                val isLocalWin = (newState.result as GameResult.Winner).symbol == localSymbol
                if (isLocalWin) audioManager.playWin() else audioManager.playLose()
                audioManager.vibrate(if (isLocalWin) 60L else 30L)
            }
            GameResult.Draw -> {
                audioManager.playDraw()
                audioManager.vibrate(40L)
            }
            else -> Unit
        }

        // En multijoueur : envoyer le coup
        if (mode != GameMode.SOLO) {
            viewModelScope.launch {
                networkRepository.send(NetworkMessage(type = MessageType.PLAY_MOVE, cellIndex = index))
                val result = newState.result
                if (result is GameResult.Winner || result == GameResult.Draw) {
                    val resultStr = when (result) {
                        is GameResult.Winner -> if (result.symbol == PlayerSymbol.X) "X_WINS" else "O_WINS"
                        GameResult.Draw      -> "DRAW"
                        else -> ""
                    }
                    networkRepository.send(
                        NetworkMessage(
                            type   = MessageType.GAME_OVER,
                            result = resultStr,
                            winner = if (result is GameResult.Winner) result.symbol.name else null
                        )
                    )
                }
            }
        }
    }

    // ─── Bot ─────────────────────────────────────────────────────────────────

    private fun scheduleBotMove() {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true) }
            delay(550L)
            val state = _uiState.value
            if (!state.isGameOver) {
                val botSymbol = localSymbol.opponent()
                val move = bot.getMove(state.board, botSymbol)
                engine.applyMove(state, move)?.let { newState ->
                    _uiState.value = newState.copy(isThinking = false)
                    audioManager.playClick()
                    when (newState.result) {
                        is GameResult.Winner -> audioManager.playLose()
                        GameResult.Draw      -> audioManager.playDraw()
                        else -> Unit
                    }
                }
            } else {
                _uiState.update { it.copy(isThinking = false) }
            }
        }
    }

    // ─── Réseau ───────────────────────────────────────────────────────────────

    private fun observeNetwork() {
        viewModelScope.launch {
            val incoming = when (mode) {
                GameMode.MULTI_HOST   -> networkRepository.serverIncoming
                GameMode.MULTI_CLIENT -> networkRepository.clientIncoming
                else -> null
            } ?: return@launch

            incoming.collect { msg ->
                when (msg.type) {
                    MessageType.PLAY_MOVE -> {
                        msg.cellIndex?.let { idx ->
                            val newState = engine.applyMove(_uiState.value, idx)
                            if (newState != null) {
                                _uiState.value = newState
                                audioManager.playClick()
                            }
                        }
                    }
                    MessageType.REMATCH -> resetBoard()
                    MessageType.DISCONNECT -> {
                        _uiState.update { it.copy(connectionError = "Adversaire déconnecté") }
                    }
                    else -> Unit
                }
            }
        }
    }

    // ─── Rejouer ─────────────────────────────────────────────────────────────

    fun rematch() {
        if (mode != GameMode.SOLO) {
            viewModelScope.launch {
                networkRepository.send(NetworkMessage(type = MessageType.REMATCH))
            }
        }
        resetBoard()
    }

    private fun resetBoard() {
        _uiState.value = engine.resetBoard(_uiState.value)
    }
}
