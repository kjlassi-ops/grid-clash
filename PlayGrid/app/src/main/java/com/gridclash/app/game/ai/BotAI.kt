package com.gridclash.app.game.ai

import com.gridclash.app.core.model.CellState
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GameResult
import com.gridclash.app.core.model.PlayerSymbol
import com.gridclash.app.game.engine.WinChecker

interface BotAI {
    fun getMove(board: List<CellState>, botSymbol: PlayerSymbol, gridSize: Int, winLength: Int): Int
}

object BotFactory {
    fun create(difficulty: Difficulty): BotAI = when (difficulty) {
        Difficulty.EASY -> EasyBot()
        Difficulty.MEDIUM -> MediumBot()
        Difficulty.HARD -> HardBot()
    }
}

class EasyBot : BotAI {
    override fun getMove(board: List<CellState>, botSymbol: PlayerSymbol, gridSize: Int, winLength: Int): Int {
        return WinChecker.emptyCells(board).random()
    }
}

class MediumBot : BotAI {
    override fun getMove(board: List<CellState>, botSymbol: PlayerSymbol, gridSize: Int, winLength: Int): Int {
        val opponent = botSymbol.opponent()
        val lines = WinChecker.buildLines(gridSize, winLength)

        findWinningMove(board, botSymbol, gridSize, winLength)?.let { return it }
        findWinningMove(board, opponent, gridSize, winLength)?.let { return it }

        val tactical = lines.asSequence()
            .mapNotNull { line ->
                val own = line.count { board[it] == CellState.from(botSymbol) }
                val opp = line.count { board[it] == CellState.from(opponent) }
                val empties = line.filter { board[it] == CellState.EMPTY }
                if (opp == 0 && own > 0 && empties.isNotEmpty()) empties.random() to own else null
            }
            .maxByOrNull { it.second }
            ?.first
        if (tactical != null) return tactical

        val center = (gridSize * gridSize) / 2
        if (gridSize % 2 == 1 && board[center] == CellState.EMPTY) return center

        return WinChecker.emptyCells(board).random()
    }

    private fun findWinningMove(board: List<CellState>, symbol: PlayerSymbol, gridSize: Int, winLength: Int): Int? {
        val cell = CellState.from(symbol)
        for (i in WinChecker.emptyCells(board)) {
            val test = board.toMutableList().also { it[i] = cell }
            if (WinChecker.check(test, gridSize, winLength) is GameResult.Winner) return i
        }
        return null
    }
}

class HardBot : BotAI {
    override fun getMove(board: List<CellState>, botSymbol: PlayerSymbol, gridSize: Int, winLength: Int): Int {
        return if (gridSize == 3) {
            minimaxMove(board, botSymbol, gridSize, winLength)
        } else {
            heuristicMove(board, botSymbol, gridSize, winLength)
        }
    }

    private fun minimaxMove(board: List<CellState>, botSymbol: PlayerSymbol, gridSize: Int, winLength: Int): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove = WinChecker.emptyCells(board).firstOrNull() ?: 0
        for (i in WinChecker.emptyCells(board)) {
            val newBoard = board.toMutableList().also { it[i] = CellState.from(botSymbol) }
            val score = minimax(newBoard, depth = 0, isMaximizing = false, botSymbol, gridSize, winLength, Int.MIN_VALUE, Int.MAX_VALUE)
            if (score > bestScore) {
                bestScore = score
                bestMove = i
            }
        }
        return bestMove
    }

    private fun heuristicMove(board: List<CellState>, botSymbol: PlayerSymbol, gridSize: Int, winLength: Int): Int {
        val medium = MediumBot().getMove(board, botSymbol, gridSize, winLength)
        val lines = WinChecker.buildLines(gridSize, winLength)
        val opponent = botSymbol.opponent()
        var bestMove = medium
        var bestScore = Int.MIN_VALUE

        for (move in WinChecker.emptyCells(board)) {
            val simulated = board.toMutableList()
            simulated[move] = CellState.from(botSymbol)
            val score = lines.sumOf { line ->
                val own = line.count { simulated[it] == CellState.from(botSymbol) }
                val opp = line.count { simulated[it] == CellState.from(opponent) }
                when {
                    own > 0 && opp == 0 -> own * own
                    opp > 0 && own == 0 -> -(opp * opp)
                    else -> 0
                }
            }
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun minimax(
        board: MutableList<CellState>,
        depth: Int,
        isMaximizing: Boolean,
        botSymbol: PlayerSymbol,
        gridSize: Int,
        winLength: Int,
        alpha: Int,
        beta: Int
    ): Int {
        when (val result = WinChecker.check(board, gridSize, winLength)) {
            is GameResult.Winner -> return if (result.symbol == botSymbol) 10 - depth else depth - 10
            GameResult.Draw -> return 0
            else -> Unit
        }

        var a = alpha
        var b = beta
        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in WinChecker.emptyCells(board)) {
                board[i] = CellState.from(botSymbol)
                best = maxOf(best, minimax(board, depth + 1, false, botSymbol, gridSize, winLength, a, b))
                board[i] = CellState.EMPTY
                a = maxOf(a, best)
                if (b <= a) break
            }
            best
        } else {
            val human = botSymbol.opponent()
            var best = Int.MAX_VALUE
            for (i in WinChecker.emptyCells(board)) {
                board[i] = CellState.from(human)
                best = minOf(best, minimax(board, depth + 1, true, botSymbol, gridSize, winLength, a, b))
                board[i] = CellState.EMPTY
                b = minOf(b, best)
                if (b <= a) break
            }
            best
        }
    }
}
