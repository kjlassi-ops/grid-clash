package com.gridclash.app.core.model

// ─── Symboles joueur ──────────────────────────────────────────────────────────

enum class PlayerSymbol { X, O;
    fun opponent(): PlayerSymbol = if (this == X) O else X
}

// ─── État d'une cellule ───────────────────────────────────────────────────────

enum class CellState {
    EMPTY, X, O;

    companion object {
        fun from(symbol: PlayerSymbol): CellState =
            if (symbol == PlayerSymbol.X) X else O
    }
}

// ─── Résultat de la partie ────────────────────────────────────────────────────

sealed class GameResult {
    object Ongoing : GameResult()
    data class Winner(val symbol: PlayerSymbol, val cells: List<Int>) : GameResult()
    object Draw : GameResult()
}

// ─── Difficulté du bot ────────────────────────────────────────────────────────

enum class Difficulty { EASY, MEDIUM, HARD;
    fun label(): String = when (this) {
        EASY   -> "Facile"
        MEDIUM -> "Moyen"
        HARD   -> "Difficile"
    }
}

// ─── Mode de jeu ─────────────────────────────────────────────────────────────

enum class GameMode { SOLO, MULTI_HOST, MULTI_CLIENT }

// ─── État UI de la partie ─────────────────────────────────────────────────────

data class GameUiState(
    val board: List<CellState>      = List(9) { CellState.EMPTY },
    val currentTurn: PlayerSymbol   = PlayerSymbol.X,
    val localSymbol: PlayerSymbol   = PlayerSymbol.X,
    val result: GameResult          = GameResult.Ongoing,
    val scoreX: Int                 = 0,
    val scoreO: Int                 = 0,
    val scoreDraw: Int              = 0,
    val isThinking: Boolean         = false,   // bot en cours de réflexion
    val opponentName: String        = "Bot",
    val moveCount: Int              = 0,
    val mode: GameMode              = GameMode.SOLO,
    val hostIp: String              = "",       // pour l'écran CreateGame
    val isWaitingForOpponent: Boolean = false,
    val connectionError: String?    = null
) {
    val isMyTurn: Boolean get() = currentTurn == localSymbol
    val isGameOver: Boolean get() = result !is GameResult.Ongoing
}
