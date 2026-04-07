package com.gridclash.app.ui.solo

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoloSetupScreen(
    onBack: () -> Unit,
    onStart: (Difficulty) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Solo Setup", color = OnSurface, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Primary)
                    }
                },
                actions = {
                    Text(
                        "GRID CLASH",
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 16.dp)
                    )
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
                    onClick = { onStart(selectedDifficulty) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
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
                        Text(
                            "DÉMARRER",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = OnPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    "CHOISIS TON DÉFI",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
                Text(
                    "SELECT\nCHALLENGE",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = OnSurface,
                    lineHeight = MaterialTheme.typography.displayMedium.fontSize * 1.0
                )
            }

            // Cartes de difficulté
            DifficultyCard(
                difficulty = Difficulty.EASY,
                emoji      = "🚀",
                subtitle   = "Parfait pour s'échauffer.",
                multiplier = "2×",
                tag        = "IA Casual",
                tagColor   = Primary,
                isSelected = selectedDifficulty == Difficulty.EASY,
                onClick    = { selectedDifficulty = Difficulty.EASY }
            )
            DifficultyCard(
                difficulty = Difficulty.MEDIUM,
                emoji      = "⚡",
                subtitle   = "L'expérience Grid Clash classique.",
                multiplier = "5×",
                tag        = "IA Adaptive",
                tagColor   = Primary,
                isSelected = selectedDifficulty == Difficulty.MEDIUM,
                onClick    = { selectedDifficulty = Difficulty.MEDIUM }
            )
            DifficultyCard(
                difficulty = Difficulty.HARD,
                emoji      = "💀",
                subtitle   = "Élite seulement. Aucune pitié.",
                multiplier = "10×",
                tag        = "IA Alpha",
                tagColor   = Error,
                isSelected = selectedDifficulty == Difficulty.HARD,
                onClick    = { selectedDifficulty = Difficulty.HARD }
            )
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
        if (isSelected) PrimaryContainer.copy(alpha = 0.6f) else Color.Transparent,
        label = "border"
    )
    val bgColor by animateColorAsState(
        if (isSelected) SurfaceContainerHigh else SurfaceContainerHigh,
        label = "bg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    difficulty.label(),
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = OnSurface
                )
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("${multiplier} Points", tagColor)
                    Chip(tag, if (isSelected) tagColor else OnSurfaceVariant)
                }
            }

            // Radio indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) PrimaryContainer else Color.Transparent)
                    .border(2.dp, if (isSelected) PrimaryContainer else OutlineVariant, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, null, tint = OnPrimaryContainer, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text     = text,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
