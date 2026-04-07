package com.gridclash.app.game.ai

import com.gridclash.app.core.model.CellState
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GameResult
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.core.model.PlayerSymbol
import com.gridclash.app.game.engine.WinChecker

// ─── Interface ────────────────────────────────────────────────────────────────

interface BotAI {
    /** Retourne l'index de la case à jouer. */
    fun getMove(board: List<CellState>, botSymbol: PlayerSymbol): Int
}

// ─── Factory ──────────────────────────────────────────────────────────────────

object BotFactory {
    fun create(difficulty: Difficulty, gridSize: GridSize = GridSize.SMALL): BotAI = when (difficulty) {
        Difficulty.EASY   -> EasyBot()
        Difficulty.MEDIUM -> MediumBot(gridSize)
        Difficulty.HARD   -> HardBot(gridSize)
    }
}

// ─── Facile : coup aléatoire ──────────────────────────────────────────────────

class EasyBot : BotAI {
    override fun getMove(board: List<CellState>, botSymbol: PlayerSymbol): Int =
        WinChecker.emptyCells(board).random()
}

// ─── Moyen : bloque / gagne, sinon stratégique ───────────────────────────────

class MediumBot(private val gridSize: GridSize) : BotAI {
    override fun getMove(board: List<CellState>, botSymbol: PlayerSymbol): Int {
        val opponent = botSymbol.opponent()

        // 1. Jouer le coup gagnant si possible
        findWinningMove(board, botSymbol)?.let { return it }

        // 2. Bloquer le coup gagnant adverse
        findWinningMove(board, opponent)?.let { return it }

        // 3. Prendre le centre si libre
        val center = (gridSize.size * gridSize.size) / 2
        if (board[center] == CellState.EMPTY) return center

        // 4. Coin libre aléatoire
        val n = gridSize.size
        val corners = listOf(0, n - 1, n * (n - 1), n * n - 1).filter { board[it] == CellState.EMPTY }
        if (corners.isNotEmpty()) return corners.random()

        // 5. N'importe quelle case libre
        return WinChecker.emptyCells(board).random()
    }

    private fun findWinningMove(board: List<CellState>, symbol: PlayerSymbol): Int? {
        val cell = CellState.from(symbol)
        for (i in WinChecker.emptyCells(board)) {
            val test = board.toMutableList().also { it[i] = cell }
            if (WinChecker.check(test, gridSize) is GameResult.Winner) return i
        }
        return null
    }
}

// ─── Difficile : Minimax avec élagage alpha-bêta + profondeur limitée ────────

class HardBot(private val gridSize: GridSize) : BotAI {

    // Profondeur max : illimitée pour 3×3, limitée pour 4×4 et 5×5
    private val maxDepth: Int = when (gridSize) {
        GridSize.SMALL  -> Int.MAX_VALUE / 2
        GridSize.MEDIUM -> 6
        GridSize.LARGE  -> 4
    }

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
        when (val result = WinChecker.check(board, gridSize)) {
            is GameResult.Winner ->
                return if (result.symbol == botSymbol) 100 - depth else depth - 100
            GameResult.Draw -> return 0
            else -> Unit
        }

        // Profondeur atteinte : évaluation heuristique
        if (depth >= maxDepth) return evaluate(board, botSymbol)

        var a = alpha
        var b = beta

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in WinChecker.emptyCells(board)) {
                board[i] = CellState.from(botSymbol)
                best = maxOf(best, minimax(board, depth + 1, false, botSymbol, a, b))
                board[i] = CellState.EMPTY
                a = maxOf(a, best)
                if (b <= a) break
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
                if (b <= a) break
            }
            best
        }
    }

    /**
     * Évaluation heuristique du plateau quand la profondeur max est atteinte.
     * Compte les lignes partiellement remplies : positif pour le bot, négatif pour l'humain.
     */
    private fun evaluate(board: List<CellState>, botSymbol: PlayerSymbol): Int {
        val botCell   = CellState.from(botSymbol)
        val humanCell = CellState.from(botSymbol.opponent())
        val lines     = WinChecker.generateWinLines(gridSize.size, gridSize.winLength)
        var score     = 0

        for (line in lines) {
            val botCount   = line.count { board[it] == botCell }
            val humanCount = line.count { board[it] == humanCell }
            // Ligne non mixte : peut encore être gagnée
            if (humanCount == 0 && botCount > 0) score += botCount * botCount
            if (botCount   == 0 && humanCount > 0) score -= humanCount * humanCount
        }
        return score
    }
}
