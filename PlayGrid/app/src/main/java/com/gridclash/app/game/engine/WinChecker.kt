package com.gridclash.app.game.engine

import com.gridclash.app.core.model.CellState
import com.gridclash.app.core.model.GameResult
import com.gridclash.app.core.model.PlayerSymbol

object WinChecker {

    fun check(board: List<CellState>, gridSize: Int, winLength: Int): GameResult {
        val lines = buildLines(gridSize, winLength)
        for (line in lines) {
            val first = board[line.first()]
            if (first == CellState.EMPTY) continue
            if (line.all { board[it] == first }) {
                return GameResult.Winner(
                    symbol = if (first == CellState.X) PlayerSymbol.X else PlayerSymbol.O,
                    cells = line
                )
            }
        }
        return if (board.none { it == CellState.EMPTY }) GameResult.Draw else GameResult.Ongoing
    }

    fun emptyCells(board: List<CellState>): List<Int> = board.indices.filter { board[it] == CellState.EMPTY }

    fun buildLines(gridSize: Int, winLength: Int): List<List<Int>> {
        val lines = mutableListOf<List<Int>>()
        for (row in 0 until gridSize) {
            for (col in 0..(gridSize - winLength)) {
                lines += (0 until winLength).map { row * gridSize + col + it }
            }
        }
        for (col in 0 until gridSize) {
            for (row in 0..(gridSize - winLength)) {
                lines += (0 until winLength).map { (row + it) * gridSize + col }
            }
        }
        for (row in 0..(gridSize - winLength)) {
            for (col in 0..(gridSize - winLength)) {
                lines += (0 until winLength).map { (row + it) * gridSize + (col + it) }
            }
        }
        for (row in 0..(gridSize - winLength)) {
            for (col in (winLength - 1) until gridSize) {
                lines += (0 until winLength).map { (row + it) * gridSize + (col - it) }
            }
        }
        return lines
    }
}
