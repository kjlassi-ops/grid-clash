package com.gridclash.app.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gridclash.app.GridClashApplication
import com.gridclash.app.core.model.*
import com.gridclash.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    mode: GameMode,
    difficulty: Difficulty,
    gridSize: GridSize,
    localName: String,
    opponentName: String,
    application: GridClashApplication,
    onBackToMenu: () -> Unit
) {
    val vm: GameViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(
                    mode, difficulty, gridSize, localName, opponentName,
                    application.container.networkRepository,
                    application.container.audioManager
                ) as T
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── TopBar ────────────────────────────────────────────────────────
            TopAppBar(
                title = { Text("GRID CLASH", color = Primary, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Icon(Icons.Default.ArrowBack, null, tint = Primary)
                    }
                },
                actions = {
                    val isMyTurn = state.isMyTurn && !state.isGameOver
                    Surface(shape = CircleShape, color = if (isMyTurn) Primary.copy(alpha = 0.15f) else SurfaceContainer) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (!state.isGameOver) {
                                Box(Modifier.size(8.dp).background(
                                    if (isMyTurn) Primary else OnSurfaceVariant, CircleShape
                                ))
                                Text(
                                    when {
                                        state.isThinking -> "Bot réfléchit..."
                                        isMyTurn         -> "Ton tour"
                                        else             -> "Tour adverse"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMyTurn) Primary else OnSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )

            // ── Scoreboard ────────────────────────────────────────────────────
            Scoreboard(state = state, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            Spacer(Modifier.weight(1f))

            // ── Plateau ───────────────────────────────────────────────────────
            GameBoard(
                state    = state,
                onClick  = vm::onCellClick,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.weight(1f))

            // ── Bot thinking ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible  = state.isThinking,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text("IA en réflexion...", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Game Over overlay ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = state.isGameOver,
            enter    = slideInVertically { it } + fadeIn(),
            exit     = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GameOverPanel(state = state, onRematch = vm::rematch, onBackToMenu = onBackToMenu)
        }
    }
}

// ─── Scoreboard ───────────────────────────────────────────────────────────────

@Composable
private fun Scoreboard(state: GameUiState, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ScoreCell(
            label    = state.localPlayerName,
            score    = if (state.localSymbol == PlayerSymbol.X) state.scoreX else state.scoreO,
            isActive = state.currentTurn == state.localSymbol,
            color    = if (state.localSymbol == PlayerSymbol.X) SymbolX else SymbolO,
            modifier = Modifier.weight(1f)
        )
        ScoreCell(
            label    = "=",
            score    = state.scoreDraw,
            isActive = false,
            color    = OnSurfaceVariant,
            modifier = Modifier.weight(0.6f)
        )
        ScoreCell(
            label    = state.opponentName,
            score    = if (state.localSymbol == PlayerSymbol.X) state.scoreO else state.scoreX,
            isActive = state.currentTurn != state.localSymbol,
            color    = if (state.localSymbol == PlayerSymbol.X) SymbolO else SymbolX,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreCell(label: String, score: Int, isActive: Boolean, color: Color, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(12.dp), color = SurfaceContainerHigh, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .then(
                    if (isActive) Modifier.border(2.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    else Modifier
                )
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant,
                maxLines = 1)
            Text(score.toString(), style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black, color = if (isActive) color else OnSurface)
        }
    }
}

// ─── Plateau dynamique ────────────────────────────────────────────────────────

@Composable
private fun GameBoard(state: GameUiState, onClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    val winningCells = (state.result as? GameResult.Winner)?.cells ?: emptyList()
    val n = state.gridSize.size

    // Taille des cellules selon la grille
    val cellSize: Dp = when (state.gridSize) {
        GridSize.SMALL  -> 88.dp
        GridSize.MEDIUM -> 72.dp
        GridSize.LARGE  -> 58.dp
    }
    val iconSize: Dp = when (state.gridSize) {
        GridSize.SMALL  -> 44.dp
        GridSize.MEDIUM -> 36.dp
        GridSize.LARGE  -> 28.dp
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 8.dp)
                .background(
                    brush = Brush.radialGradient(listOf(Primary.copy(alpha = 0.04f), Color.Transparent)),
                    shape = RoundedCornerShape(32.dp)
                )
        )
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SurfaceContainerLow.copy(alpha = 0.7f),
            modifier = Modifier.padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until n) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (col in 0 until n) {
                            val index = row * n + col
                            GameCell(
                                cell        = state.board[index],
                                isWinning   = index in winningCells,
                                isClickable = state.board[index] == CellState.EMPTY
                                              && state.isMyTurn && !state.isGameOver && !state.isThinking,
                                cellSize    = cellSize,
                                iconSize    = iconSize,
                                onClick     = { onClick(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCell(
    cell: CellState,
    isWinning: Boolean,
    isClickable: Boolean,
    cellSize: Dp,
    iconSize: Dp,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (cell != CellState.EMPTY) 1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cell_scale"
    )

    Box(
        modifier = Modifier
            .size(cellSize)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isWinning) WinningCellBg else SurfaceContainerHighest)
            .then(
                if (isWinning) Modifier.border(1.5.dp, WinningCellBorder, RoundedCornerShape(14.dp))
                else Modifier
            )
            .clickable(enabled = isClickable, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when (cell) {
            CellState.X -> Icon(Icons.Default.Close, "X", tint = SymbolX, modifier = Modifier.size(iconSize))
            CellState.O -> Icon(Icons.Outlined.Circle, "O", tint = SymbolO, modifier = Modifier.size(iconSize))
            CellState.EMPTY -> if (isClickable) {
                Box(Modifier.size(16.dp).background(Primary.copy(alpha = 0.08f), CircleShape))
            }
        }
    }
}

// ─── Game Over Panel ──────────────────────────────────────────────────────────

@Composable
private fun GameOverPanel(state: GameUiState, onRematch: () -> Unit, onBackToMenu: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = SurfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val (title, subtitle) = when (val r = state.result) {
                is GameResult.Winner -> {
                    val isLocalWinner = r.symbol == state.localSymbol
                    val winnerName    = if (isLocalWinner) state.localPlayerName else state.opponentName
                    if (isLocalWinner) Pair("VICTOIRE", "$winnerName a dominé la grille !")
                    else Pair("DÉFAITE", "$winnerName a gagné cette fois.")
                }
                GameResult.Draw -> Pair("ÉGALITÉ", "Match nul — revanche ?")
                else -> Pair("", "")
            }

            Text(
                title,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = when (state.result) {
                    is GameResult.Winner ->
                        if ((state.result as GameResult.Winner).symbol == state.localSymbol) Primary else Error
                    else -> OnSurface
                },
                textAlign = TextAlign.Center
            )

            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("Coups", "${state.moveCount}")
                StatChip(state.localPlayerName.take(6), "${if (state.localSymbol == PlayerSymbol.X) state.scoreX else state.scoreO}")
                StatChip(state.opponentName.take(6),    "${if (state.localSymbol == PlayerSymbol.X) state.scoreO else state.scoreX}")
                StatChip("=", "${state.scoreDraw}")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onRematch,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(listOf(Primary, PrimaryContainer)), RoundedCornerShape(50)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Rejouer", color = OnPrimaryContainer, fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge)
                }
            }
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
            ) {
                Text("Menu principal", color = OnSurface, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = SurfaceContainerHighest) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}
