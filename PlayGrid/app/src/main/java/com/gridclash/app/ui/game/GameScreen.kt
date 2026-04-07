package com.gridclash.app.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gridclash.app.GridClashApplication
import com.gridclash.app.core.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(config: GameConfig, application: GridClashApplication, onBackToMenu: () -> Unit) {
    val vm: GameViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(config, application.container.networkRepository, application.container.audioManager) as T
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        TopAppBar(
            title = { Text("${state.localPlayerName} vs ${state.opponentName}") },
            navigationIcon = { IconButton(onClick = onBackToMenu) { Icon(Icons.Default.ArrowBack, null) } },
            actions = { Text(state.networkStatus.name, modifier = Modifier.padding(end = 12.dp)) }
        )

        Text("Grille: ${state.gridSize.label} - Victoire à ${state.winLength}", style = MaterialTheme.typography.bodyMedium)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("${state.localPlayerName}: ${if (state.localSymbol == PlayerSymbol.X) state.scoreX else state.scoreO}") })
            AssistChip(onClick = {}, label = { Text("Nuls: ${state.scoreDraw}") })
            AssistChip(onClick = {}, label = { Text("${state.opponentName}: ${if (state.localSymbol == PlayerSymbol.X) state.scoreO else state.scoreX}") })
        }

        Spacer(Modifier.height(12.dp))
        GameBoard(state = state, onClick = vm::onCellClick)

        AnimatedVisibility(visible = state.isGameOver, enter = fadeIn(), exit = fadeOut()) {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    when (val r = state.result) {
                        is GameResult.Winner -> if (r.symbol == state.localSymbol) "Victoire" else "Défaite"
                        GameResult.Draw -> "Égalité"
                        else -> ""
                    },
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = vm::rematch, modifier = Modifier.fillMaxWidth()) { Text("Rejouer") }
            }
        }
    }
}

@Composable
private fun GameBoard(state: GameUiState, onClick: (Int) -> Unit) {
    val winningCells = (state.result as? GameResult.Winner)?.cells ?: emptyList()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(state.gridSize.size) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(state.gridSize.size) { col ->
                    val index = row * state.gridSize.size + col
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = if (index in winningCells) 8.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable(enabled = state.board[index] == CellState.EMPTY && state.isMyTurn && !state.isGameOver && !state.isThinking) {
                                onClick(index)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                            when (state.board[index]) {
                                CellState.X -> Icon(Icons.Default.Close, contentDescription = null)
                                CellState.O -> Icon(Icons.Outlined.Circle, contentDescription = null)
                                CellState.EMPTY -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}
