package com.gridclash.app.core.model

import androidx.compose.ui.graphics.Color
import com.gridclash.app.ui.theme.SymbolO
import com.gridclash.app.ui.theme.SymbolX

enum class PlayerSymbol { X, O;
    fun opponent(): PlayerSymbol = if (this == X) O else X
}

enum class CellState {
    EMPTY, X, O;

    companion object {
        fun from(symbol: PlayerSymbol): CellState = if (symbol == PlayerSymbol.X) X else O
    }
}

sealed class GameResult {
    object Ongoing : GameResult()
    data class Winner(val symbol: PlayerSymbol, val cells: List<Int>) : GameResult()
    object Draw : GameResult()
}

enum class Difficulty { EASY, MEDIUM, HARD;
    fun label(): String = when (this) {
        EASY   -> "Facile"
        MEDIUM -> "Moyen"
        HARD   -> "Difficile"
    }
}

enum class GameMode { SOLO, MULTI_HOST, MULTI_CLIENT }

enum class GridSize(val size: Int, val label: String) {
    THREE(3, "3x3"),
    FOUR(4, "4x4"),
    FIVE(5, "5x5")
}

enum class ThemePreference { SYSTEM, LIGHT, DARK }

enum class NetworkStatus { IDLE, WAITING, CONNECTED, DISCONNECTED, ERROR }

data class PlayerStyle(
    val xColor: Color = SymbolX,
    val oColor: Color = SymbolO
)

data class GameConfig(
    val mode: GameMode = GameMode.SOLO,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val gridSize: GridSize = GridSize.THREE,
    val winLength: Int = 3,
    val localPlayerName: String = "Joueur",
    val remotePlayerName: String = "Adversaire"
)

data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val playerStyle: PlayerStyle = PlayerStyle(),
    val lastLocalPseudo: String = "Joueur",
    val lastHostIp: String = "",
    val lastGridSize: GridSize = GridSize.THREE,
    val lastDifficulty: Difficulty = Difficulty.MEDIUM
)

data class GameUiState(
    val board: List<CellState> = List(9) { CellState.EMPTY },
    val currentTurn: PlayerSymbol = PlayerSymbol.X,
    val localSymbol: PlayerSymbol = PlayerSymbol.X,
    val result: GameResult = GameResult.Ongoing,
    val scoreX: Int = 0,
    val scoreO: Int = 0,
    val scoreDraw: Int = 0,
    val isThinking: Boolean = false,
    val localPlayerName: String = "Joueur",
    val opponentName: String = "Bot",
    val moveCount: Int = 0,
    val mode: GameMode = GameMode.SOLO,
    val gridSize: GridSize = GridSize.THREE,
    val winLength: Int = 3,
    val isWaitingForOpponent: Boolean = false,
    val connectionError: String? = null,
    val networkStatus: NetworkStatus = NetworkStatus.IDLE
) {
    val isMyTurn: Boolean get() = currentTurn == localSymbol
    val isGameOver: Boolean get() = result !is GameResult.Ongoing
}
