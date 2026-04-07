package com.gridclash.app.game.engine

import com.gridclash.app.core.model.CellState
import com.gridclash.app.core.model.GameResult
import com.gridclash.app.core.model.GameUiState
import com.gridclash.app.core.model.PlayerSymbol

class GameEngine {

    /**
     * Applique un coup sur le plateau. Retourne le nouvel état ou null si le coup est invalide.
     */
    fun applyMove(state: GameUiState, cellIndex: Int): GameUiState? {
        if (state.isGameOver) return null
        val totalCells = state.gridSize.size * state.gridSize.size
        if (cellIndex !in 0 until totalCells) return null
        if (state.board[cellIndex] != CellState.EMPTY) return null

        val newBoard = state.board.toMutableList()
        newBoard[cellIndex] = CellState.from(state.currentTurn)

        val result = WinChecker.check(newBoard, state.gridSize)
        val nextTurn = if (result is GameResult.Ongoing) state.currentTurn.opponent()
                       else state.currentTurn

        val (scoreX, scoreO, scoreDraw) = when (result) {
            is GameResult.Winner -> if (result.symbol == PlayerSymbol.X)
                Triple(state.scoreX + 1, state.scoreO, state.scoreDraw)
            else Triple(state.scoreX, state.scoreO + 1, state.scoreDraw)
            GameResult.Draw    -> Triple(state.scoreX, state.scoreO, state.scoreDraw + 1)
            GameResult.Ongoing -> Triple(state.scoreX, state.scoreO, state.scoreDraw)
        }

        return state.copy(
            board       = newBoard,
            currentTurn = nextTurn,
            result      = result,
            moveCount   = state.moveCount + 1,
            scoreX      = scoreX,
            scoreO      = scoreO,
            scoreDraw   = scoreDraw
        )
    }

    /** Remet le plateau à zéro sans toucher aux scores ni aux noms. */
    fun resetBoard(state: GameUiState): GameUiState = state.copy(
        board       = List(state.gridSize.size * state.gridSize.size) { CellState.EMPTY },
        currentTurn = PlayerSymbol.X,
        result      = GameResult.Ongoing,
        moveCount   = 0,
        isThinking  = false
    )
}
