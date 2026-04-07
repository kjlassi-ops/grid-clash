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

// ─── Taille de grille ─────────────────────────────────────────────────────────

enum class GridSize(val size: Int, val winLength: Int, val label: String) {
    SMALL(3, 3, "3×3"),
    MEDIUM(4, 4, "4×4"),
    LARGE(5, 4, "5×5")
}

// ─── État UI de la partie ─────────────────────────────────────────────────────

data class GameUiState(
    val gridSize: GridSize               = GridSize.SMALL,
    val board: List<CellState>           = List(GridSize.SMALL.size * GridSize.SMALL.size) { CellState.EMPTY },
    val currentTurn: PlayerSymbol        = PlayerSymbol.X,
    val localSymbol: PlayerSymbol        = PlayerSymbol.X,
    val result: GameResult               = GameResult.Ongoing,
    val scoreX: Int                      = 0,
    val scoreO: Int                      = 0,
    val scoreDraw: Int                   = 0,
    val isThinking: Boolean              = false,
    val localPlayerName: String          = "Toi",
    val opponentName: String             = "Bot",
    val moveCount: Int                   = 0,
    val mode: GameMode                   = GameMode.SOLO,
    val hostIp: String                   = "",
    val isWaitingForOpponent: Boolean    = false,
    val connectionError: String?         = null
) {
    val isMyTurn: Boolean get() = currentTurn == localSymbol
    val isGameOver: Boolean get() = result !is GameResult.Ongoing
}
