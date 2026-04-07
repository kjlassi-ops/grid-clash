package com.gridclash.app.ui.solo

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gridclash.app.GridClashApplication
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloSetupScreen(
    application: GridClashApplication,
    onBack: () -> Unit,
    onStart: (Difficulty, GridSize, String) -> Unit
) {
    // Charger les préférences sauvegardées
    val prefs by application.container.preferencesRepository.preferences.collectAsStateWithLifecycle(
        initialValue = com.gridclash.app.data.UserPreferences()
    )

    var selectedDifficulty by remember(prefs.difficulty) { mutableStateOf(prefs.difficulty) }
    var selectedGridSize   by remember(prefs.gridSize)   { mutableStateOf(prefs.gridSize) }
    var playerName         by remember(prefs.playerName) { mutableStateOf(prefs.playerName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solo Setup", color = OnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Primary)
                    }
                },
                actions = {
                    Text("GRID CLASH", style = MaterialTheme.typography.titleLarge,
                        color = Primary, fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow.copy(alpha = 0.9f))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Button(
                    onClick = { onStart(selectedDifficulty, selectedGridSize, playerName.trim().ifBlank { "Joueur" }) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(Primary, PrimaryContainer)
                                ),
                                shape = RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("DÉMARRER", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black, color = OnPrimaryContainer)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("CHOISIS TON DÉFI", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Text("SELECT\nCHALLENGE", style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black, color = OnSurface,
                    lineHeight = MaterialTheme.typography.displayMedium.fontSize * 1.0)
            }

            // ── Pseudo ────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TON PSEUDO", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { if (it.length <= 20) playerName = it },
                    placeholder = { Text("ex: Joueur1", color = Outline) },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor      = Primary,
                        unfocusedTextColor    = OnSurface,
                        focusedBorderColor    = Primary,
                        unfocusedBorderColor  = OutlineVariant,
                        focusedContainerColor    = SurfaceContainerHighest,
                        unfocusedContainerColor  = SurfaceContainerHighest
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ── Taille de grille ──────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TAILLE DE GRILLE", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GridSize.entries.forEach { gs ->
                        GridSizeChip(
                            gridSize   = gs,
                            isSelected = selectedGridSize == gs,
                            onClick    = { selectedGridSize = gs },
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Difficulté ────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("DIFFICULTÉ", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                DifficultyCard(Difficulty.EASY,   "🚀", "Parfait pour s'échauffer.",          "2×",  "IA Casual",   Primary, selectedDifficulty == Difficulty.EASY)   { selectedDifficulty = Difficulty.EASY }
                DifficultyCard(Difficulty.MEDIUM, "⚡", "L'expérience Grid Clash classique.",  "5×",  "IA Adaptive", Primary, selectedDifficulty == Difficulty.MEDIUM) { selectedDifficulty = Difficulty.MEDIUM }
                DifficultyCard(Difficulty.HARD,   "💀", "Élite seulement. Aucune pitié.",       "10×", "IA Alpha",    Error,   selectedDifficulty == Difficulty.HARD)   { selectedDifficulty = Difficulty.HARD }
            }
        }
    }
}

@Composable
private fun GridSizeChip(
    gridSize: GridSize,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (isSelected) Primary else OutlineVariant.copy(alpha = 0.4f), label = "gs_border"
    )
    val bgColor by animateColorAsState(
        if (isSelected) Primary.copy(alpha = 0.12f) else SurfaceContainerHigh, label = "gs_bg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(gridSize.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Primary else OnSurface)
            Text("victoire ${gridSize.winLength}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Primary.copy(alpha = 0.7f) else OnSurfaceVariant)
        }
    }
}

@Composable
private fun DifficultyCard(
    difficulty: Difficulty,
    emoji: String,
    subtitle: String,
    multiplier: String,
    tag: String,
    tagColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isSelected) PrimaryContainer.copy(alpha = 0.6f) else Color.Transparent, label = "border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerHigh)
            .border(if (isSelected) 2.dp else 0.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(difficulty.label(), style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = OnSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("${multiplier} Points", tagColor)
                    Chip(tag, if (isSelected) tagColor else OnSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) PrimaryContainer else Color.Transparent)
                    .border(2.dp, if (isSelected) PrimaryContainer else OutlineVariant, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Icon(Icons.Default.Check, null, tint = OnPrimaryContainer, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}
