package com.gridclash.app.game.ai

import com.gridclash.app.core.model.CellState
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.PlayerSymbol
import com.gridclash.app.game.engine.WinChecker

// ─── Interface ────────────────────────────────────────────────────────────────

interface BotAI {
    /** Retourne l'index de la case à jouer (0-8). */
    fun getMove(board: List<CellState>, botSymbol: PlayerSymbol): Int
}

// ─── Factory ──────────────────────────────────────────────────────────────────

object BotFactory {
    fun create(difficulty: Difficulty): BotAI = when (difficulty) {
        Difficulty.EASY   -> EasyBot()
        Difficulty.MEDIUM -> MediumBot()
        Difficulty.HARD   -> HardBot()
    }
}

// ─── Facile : coup aléatoire ──────────────────────────────────────────────────

class EasyBot : BotAI {
    override fun getMove(board: List<CellState>, botSymbol: PlayerSymbol): Int =
        WinChecker.emptyCells(board).random()
}

// ─── Moyen : bloque / gagne, sinon aléatoire ─────────────────────────────────

class MediumBot : BotAI {
    override fun getMove(board: List<CellState>, botSymbol: PlayerSymbol): Int {
        val opponent = botSymbol.opponent()

        // 1. Jouer le coup gagnant si possible
        findWinningMove(board, botSymbol)?.let { return it }

        // 2. Bloquer le coup gagnant adverse
        findWinningMove(board, opponent)?.let { return it }

        // 3. Prendre le centre si libre
        if (board[4] == CellState.EMPTY) return 4

        // 4. Coin libre aléatoire
        val corners = listOf(0, 2, 6, 8).filter { board[it] == CellState.EMPTY }
        if (corners.isNotEmpty()) return corners.random()

        // 5. N'importe quelle case libre
        return WinChecker.emptyCells(board).random()
    }

    private fun findWinningMove(board: List<CellState>, symbol: PlayerSymbol): Int? {
        val cell = CellState.from(symbol)
        for (i in WinChecker.emptyCells(board)) {
            val test = board.toMutableList().also { it[i] = cell }
            if (WinChecker.check(test) is com.gridclash.app.core.model.GameResult.Winner) return i
        }
        return null
    }
}

// ─── Difficile : Minimax avec élagage alpha-bêta ─────────────────────────────

class HardBot : BotAI {
    override fun getMove(board: List<CellState>, botSymbol: PlayerSymbol): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove  = WinChecker.emptyCells(board).firstOrNull() ?: 0

        for (i in WinChecker.emptyCells(board)) {
            val newBoard = board.toMutableList().also { it[i] = CellState.from(botSymbol) }
            val score = minimax(newBoard, depth = 0, isMaximizing = false,
                                botSymbol = botSymbol, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE)
            if (score > bestScore) { bestScore = score; bestMove = i }
        }
        return bestMove
    }

    private fun minimax(
        board: MutableList<CellState>,
        depth: Int,
        isMaximizing: Boolean,
        botSymbol: PlayerSymbol,
        alpha: Int,
        beta: Int
    ): Int {
        when (val result = WinChecker.check(board)) {
            is com.gridclash.app.core.model.GameResult.Winner ->
                return if (result.symbol == botSymbol) 10 - depth else depth - 10
            com.gridclash.app.core.model.GameResult.Draw -> return 0
            else -> Unit
        }

        var a = alpha
        var b = beta

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in WinChecker.emptyCells(board)) {
                board[i] = CellState.from(botSymbol)
                best = maxOf(best, minimax(board, depth + 1, false, botSymbol, a, b))
                board[i] = CellState.EMPTY
                a = maxOf(a, best)
                if (b <= a) break // élagage beta
            }
            best
        } else {
            val humanSymbol = botSymbol.opponent()
            var best = Int.MAX_VALUE
            for (i in WinChecker.emptyCells(board)) {
                board[i] = CellState.from(humanSymbol)
                best = minOf(best, minimax(board, depth + 1, true, botSymbol, a, b))
                board[i] = CellState.EMPTY
                b = minOf(b, best)
                if (b <= a) break // élagage alpha
            }
            best
        }
    }
}
