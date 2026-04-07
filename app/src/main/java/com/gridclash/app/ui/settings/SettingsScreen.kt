package com.gridclash.app.ui.settings

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gridclash.app.GridClashApplication
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(application: GridClashApplication, onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(
                    application.container.preferencesRepository,
                    application.container.audioManager
                ) as T
            }
        }
    )
    val prefs by vm.preferences.collectAsStateWithLifecycle()

    // Champ pseudo local (évite de sauvegarder à chaque frappe)
    var nameField by remember(prefs.playerName) { mutableStateOf(prefs.playerName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres", color = OnSurface, fontWeight = FontWeight.Bold) },
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
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Personnaliser", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold, color = OnSurface.copy(alpha = 0.9f))
            Text("Adapte ton environnement de combat.",
                style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp))

            // ── Profil ────────────────────────────────────────────────────────
            SectionLabel("Profil")
            OutlinedTextField(
                value = nameField,
                onValueChange = { if (it.length <= 20) nameField = it },
                label = { Text("Pseudo", color = OnSurfaceVariant) },
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
            // Sauvegarder quand le champ perd le focus (via bouton Save explicite)
            Button(
                onClick = { vm.savePlayerName(nameField.trim().ifBlank { "Joueur" }) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh)
            ) {
                Text("Sauvegarder le pseudo", color = Primary, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(4.dp))

            // ── Grille par défaut ─────────────────────────────────────────────
            SectionLabel("Grille par défaut")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GridSize.entries.forEach { gs ->
                    val isSelected = prefs.gridSize == gs
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Primary.copy(alpha = 0.12f) else SurfaceContainerHigh)
                            .border(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) Primary else OutlineVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { vm.saveGridSize(gs) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(gs.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Primary else OnSurface)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Difficulté par défaut ─────────────────────────────────────────
            SectionLabel("Difficulté par défaut (solo)")
            Surface(shape = RoundedCornerShape(16.dp), color = SurfaceContainerLow) {
                Column {
                    Difficulty.entries.forEachIndexed { idx, d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.saveDifficulty(d) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(d.label(), style = MaterialTheme.typography.bodyLarge,
                                color = if (prefs.difficulty == d) Primary else OnSurface,
                                fontWeight = if (prefs.difficulty == d) FontWeight.Bold else FontWeight.Normal)
                            if (prefs.difficulty == d) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Primary.copy(alpha = 0.12f)) {
                                    Text("✓", style = MaterialTheme.typography.labelSmall, color = Primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                        if (idx < Difficulty.entries.size - 1) HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Son & Musique ─────────────────────────────────────────────────
            SectionLabel("Son & Musique")
            ToggleRow("Effets sonores", "Sons des actions en jeu",
                prefs.soundEnabled)     { vm.setSoundEnabled(it) }
            ToggleRow("Musique",         "Piste synthwave d'ambiance",
                prefs.musicEnabled)     { vm.setMusicEnabled(it) }
            ToggleRow("Vibrations",      "Retour haptique sur les coups",
                prefs.vibrationEnabled) { vm.setVibrationEnabled(it) }

            Spacer(Modifier.height(8.dp))

            // ── À propos ──────────────────────────────────────────────────────
            SectionLabel("À propos")
            InfoRow("Version", "2.0.0")
            InfoRow("Package", "com.gridclash.app")
            InfoRow("Build", "Debug")

            Spacer(Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainerHigh.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("GRID CLASH CORE", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text("Version 2.0.0", style = MaterialTheme.typography.bodyLarge,
                        color = Primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = SurfaceContainerLow) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold, color = OnSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor   = PrimaryContainer,
                    checkedThumbColor   = OnPrimaryContainer,
                    uncheckedTrackColor = SurfaceContainerHighest,
                    uncheckedThumbColor = OnSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = SurfaceContainerLow) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(value, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold, color = OnSurface)
                Icon(Icons.Default.ChevronRight, null, tint = OutlineVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}
