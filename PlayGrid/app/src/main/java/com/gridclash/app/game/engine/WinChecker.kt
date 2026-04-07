package com.gridclash.app.game.engine

import com.gridclash.app.core.model.CellState
import com.gridclash.app.core.model.GameResult
import com.gridclash.app.core.model.PlayerSymbol

object WinChecker {

    // Toutes les combinaisons gagnantes (indices du plateau 0..8)
    private val WIN_LINES = listOf(
        listOf(0, 1, 2), // ligne 1
        listOf(3, 4, 5), // ligne 2
        listOf(6, 7, 8), // ligne 3
        listOf(0, 3, 6), // colonne 1
        listOf(1, 4, 7), // colonne 2
        listOf(2, 5, 8), // colonne 3
        listOf(0, 4, 8), // diagonale \
        listOf(2, 4, 6)  // diagonale /
    )

    /**
     * Analyse le plateau et retourne le résultat actuel.
     */
    fun check(board: List<CellState>): GameResult {
        for (line in WIN_LINES) {
            val (a, b, c) = line
            val cell = board[a]
            if (cell != CellState.EMPTY && cell == board[b] && cell == board[c]) {
                val winner = if (cell == CellState.X) PlayerSymbol.X else PlayerSymbol.O
                return GameResult.Winner(winner, line)
            }
        }
        return if (board.none { it == CellState.EMPTY }) GameResult.Draw
        else GameResult.Ongoing
    }

    /** Retourne les cases vides. */
    fun emptyCells(board: List<CellState>): List<Int> =
        board.indices.filter { board[it] == CellState.EMPTY }
}
