package com.gridclash.app.game.engine

import com.gridclash.app.core.model.CellState
import com.gridclash.app.core.model.GameResult
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.core.model.PlayerSymbol

object WinChecker {

    /**
     * Génère toutes les lignes gagnantes possibles pour une grille de taille [gridSize]
     * avec une longueur de victoire [winLength].
     */
    fun generateWinLines(gridSize: Int, winLength: Int): List<List<Int>> {
        val lines = mutableListOf<List<Int>>()

        // Lignes horizontales
        for (row in 0 until gridSize) {
            for (startCol in 0..gridSize - winLength) {
                lines.add((startCol until startCol + winLength).map { col -> row * gridSize + col })
            }
        }

        // Lignes verticales
        for (col in 0 until gridSize) {
            for (startRow in 0..gridSize - winLength) {
                lines.add((startRow until startRow + winLength).map { row -> row * gridSize + col })
            }
        }

        // Diagonales ↘
        for (startRow in 0..gridSize - winLength) {
            for (startCol in 0..gridSize - winLength) {
                lines.add((0 until winLength).map { k -> (startRow + k) * gridSize + (startCol + k) })
            }
        }

        // Diagonales ↙
        for (startRow in 0..gridSize - winLength) {
            for (startCol in winLength - 1 until gridSize) {
                lines.add((0 until winLength).map { k -> (startRow + k) * gridSize + (startCol - k) })
            }
        }

        return lines
    }

    /**
     * Analyse le plateau et retourne le résultat actuel.
     */
    fun check(board: List<CellState>, gridSize: Int, winLength: Int): GameResult {
        val lines = generateWinLines(gridSize, winLength)
        for (line in lines) {
            val first = board[line[0]]
            if (first != CellState.EMPTY && line.all { board[it] == first }) {
                val winner = if (first == CellState.X) PlayerSymbol.X else PlayerSymbol.O
                return GameResult.Winner(winner, line)
            }
        }
        return if (board.none { it == CellState.EMPTY }) GameResult.Draw
        else GameResult.Ongoing
    }

    fun check(board: List<CellState>, gridSize: GridSize): GameResult =
        check(board, gridSize.size, gridSize.winLength)

    /** Retourne les cases vides. */
    fun emptyCells(board: List<CellState>): List<Int> =
        board.indices.filter { board[it] == CellState.EMPTY }
}
