package com.gridclash.app.ui.solo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GameConfig
import com.gridclash.app.core.model.GameMode
import com.gridclash.app.core.model.GridSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloSetupScreen(
    initialPseudo: String,
    initialGridSize: GridSize,
    initialDifficulty: Difficulty,
    onBack: () -> Unit,
    onStart: (GameConfig) -> Unit
) {
    var pseudo by remember { mutableStateOf(initialPseudo) }
    var gridSize by remember { mutableStateOf(initialGridSize) }
    var difficulty by remember { mutableStateOf(initialDifficulty) }
    val winLength = if (gridSize == GridSize.FIVE) 4 else 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuration Solo", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = pseudo,
                onValueChange = { pseudo = it.take(20) },
                label = { Text("Pseudo") },
                supportingText = { Text("Minimum 2 caractères") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Taille de grille")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GridSize.entries.forEach { option ->
                    FilterChip(
                        selected = option == gridSize,
                        onClick = { gridSize = option },
                        label = { Text(option.label) }
                    )
                }
            }

            Text("Difficulté IA")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { option ->
                    FilterChip(
                        selected = option == difficulty,
                        onClick = { difficulty = option },
                        label = { Text(option.label()) }
                    )
                }
            }

            Text("Condition de victoire: $winLength alignés")

            Button(
                onClick = {
                    onStart(
                        GameConfig(
                            mode = GameMode.SOLO,
                            difficulty = difficulty,
                            gridSize = gridSize,
                            winLength = winLength,
                            localPlayerName = pseudo.trim(),
                            remotePlayerName = "Bot (${difficulty.label()})"
                        )
                    )
                },
                enabled = pseudo.trim().length >= 2,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Démarrer") }
        }
    }
}
