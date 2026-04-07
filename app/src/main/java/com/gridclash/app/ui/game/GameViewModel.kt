package com.gridclash.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val engine = GameEngine()
    private val bot    = BotFactory.create(difficulty)

    // Le symbole du joueur local (X pour solo et host, O pour client)
    private val localSymbol: PlayerSymbol = when (mode) {
        GameMode.SOLO, GameMode.MULTI_HOST -> PlayerSymbol.X
        GameMode.MULTI_CLIENT              -> PlayerSymbol.O
    }

    private val _uiState = MutableStateFlow(
        GameUiState(
            mode        = mode,
            localSymbol = localSymbol,
            opponentName = when (mode) {
                GameMode.SOLO         -> "Bot (${difficulty.label()})"
                GameMode.MULTI_HOST   -> "Adversaire"
                GameMode.MULTI_CLIENT -> "Hôte"
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

        playMove(index)

        // En solo, déclencher le bot
        if (mode == GameMode.SOLO && !_uiState.value.isGameOver) {
            scheduleBotMove()
        }
    }

    private fun playMove(index: Int) {
        val newState = engine.applyMove(_uiState.value, index) ?: return
        _uiState.value = newState

        // En multijoueur : envoyer le coup à l'adversaire
        if (mode != GameMode.SOLO) {
            viewModelScope.launch {
                networkRepository.send(NetworkMessage(type = MessageType.PLAY_MOVE, cellIndex = index))

                // Notifier fin de partie si besoin
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
            delay(550L) // délai UX
            val state = _uiState.value
            if (!state.isGameOver) {
                val botSymbol = localSymbol.opponent()
                val move = bot.getMove(state.board, botSymbol)
                engine.applyMove(state, move)?.let { newState ->
                    _uiState.value = newState.copy(isThinking = false)
                }
            } else {
                _uiState.update { it.copy(isThinking = false) }
            }
        }
    }

    // ─── Réseau : écoute des messages adversaire ─────────────────────────────

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
                            if (newState != null) _uiState.value = newState
                        }
                    }
                    MessageType.REMATCH -> {
                        resetBoard()
                    }
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
