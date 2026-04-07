package com.gridclash.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gridclash.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // État local simple (persister avec DataStore est une évolution V2)
    var soundEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

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
            Text("Personnaliser",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurface.copy(alpha = 0.9f)
            )
            Text("Adapte ton environnement de combat.",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SectionLabel("Son & Musique")

            ToggleRow(
                title    = "Effets sonores",
                subtitle = "Sons des actions en jeu",
                checked  = soundEnabled,
                onToggle = { soundEnabled = it }
            )
            ToggleRow(
                title    = "Musique",
                subtitle = "Piste synthwave d'ambiance",
                checked  = musicEnabled,
                onToggle = { musicEnabled = it }
            )
            ToggleRow(
                title    = "Vibrations",
                subtitle = "Retour haptique sur les coups",
                checked  = vibrationEnabled,
                onToggle = { vibrationEnabled = it }
            )

            Spacer(Modifier.height(8.dp))
            SectionLabel("À propos")

            InfoRow(title = "Version", value = "1.0.0 — MVP")
            InfoRow(title = "Package", value = "com.gridclash.app")
            InfoRow(title = "Build", value = "Debug")

            Spacer(Modifier.height(24.dp))

            // Bandeau version centré
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainerHigh.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("GRID CLASH CORE", style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant)
                    Text("Version 1.0.0-MVP", style = MaterialTheme.typography.bodyLarge,
                        color = Primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = OnSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold, color = OnSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant)
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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant)
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
