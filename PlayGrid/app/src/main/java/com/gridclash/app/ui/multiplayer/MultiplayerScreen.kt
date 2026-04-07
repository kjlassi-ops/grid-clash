package com.gridclash.app.ui.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gridclash.app.GridClashApplication
import com.gridclash.app.core.model.Difficulty
import com.gridclash.app.core.model.GridSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(
    application: GridClashApplication,
    onBack: () -> Unit,
    onGameStartHost: () -> Unit,
    onGameStartClient: () -> Unit
) {
    val context = LocalContext.current
    val vm: MultiplayerViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MultiplayerViewModel(
                    networkRepository = application.container.networkRepository,
                    preferencesRepository = application.container.preferencesRepository,
                    context = context
                ) as T
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.gameStarted, state.role) {
        if (state.gameStarted) {
            if (state.role == "HOST") onGameStartHost() else if (state.role == "CLIENT") onGameStartClient()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multijoueur") },
                navigationIcon = { IconButton(onClick = { vm.stopHosting(); onBack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("État réseau: ${state.networkStatus.name}")
            if (state.error != null) Text(state.error ?: "", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = state.localPseudo,
                onValueChange = vm::updatePseudo,
                label = { Text("Pseudo") },
                supportingText = { Text("Visible dans le lobby et la partie") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Paramètres de partie")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GridSize.entries.forEach { option ->
                    FilterChip(selected = option == state.gridSize, onClick = { vm.updateGridSize(option) }, label = { Text(option.label) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { option ->
                    FilterChip(selected = option == state.difficulty, onClick = { vm.updateDifficulty(option) }, label = { Text(option.label()) })
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Hôte")
                    Text("IP locale: ${state.localIp}")
                    Text("Lobby: ${state.localPseudo} vs ${state.remotePseudo}")
                    Button(onClick = vm::startHosting, enabled = !state.isHosting && !state.isConnecting, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.isHosting) "En attente..." else "Héberger")
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Client")
                    OutlinedTextField(
                        value = state.inputIp,
                        onValueChange = vm::updateInputIp,
                        label = { Text("IP hôte") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = vm::joinGame, enabled = !state.isHosting && !state.isConnecting, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.isConnecting) "Connexion..." else "Connecter")
                    }
                }
            }
        }
    }
}
