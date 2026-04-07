package com.gridclash.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gridclash.app.core.model.AppSettings
import com.gridclash.app.core.model.ThemePreference
import com.gridclash.app.data.AppPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: AppPreferencesRepository, onBack: () -> Unit) {
    val scope = CoroutineScope(Dispatchers.Main)
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
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
            Text("Personnalisation")
            Text("Audio: utilisez des packs royalty-free (Freesound/Pixabay/OpenGameArt) dans res/raw.")

            Text("Thème")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePreference.entries.forEach { option ->
                    FilterChip(
                        selected = settings.theme == option,
                        onClick = { scope.launch { repository.setTheme(option) } },
                        label = { Text(option.name) }
                    )
                }
            }

            SwitchRow("Effets sonores", settings.soundEnabled) { scope.launch { repository.setSoundEnabled(it) } }
            SwitchRow("Musique", settings.musicEnabled) { scope.launch { repository.setMusicEnabled(it) } }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
