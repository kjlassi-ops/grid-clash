package com.gridclash.app.ui.multiplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gridclash.app.GridClashApplication
import com.gridclash.app.core.model.GridSize
import com.gridclash.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(
    application: GridClashApplication,
    onBack: () -> Unit,
    onGameStartHost: (GridSize, String, String) -> Unit,
    onGameStartClient: (GridSize, String, String) -> Unit
) {
    val context = LocalContext.current

    // Charger les préférences
    val prefs by application.container.preferencesRepository.preferences.collectAsStateWithLifecycle(
        initialValue = com.gridclash.app.data.UserPreferences()
    )

    val vm: MultiplayerViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MultiplayerViewModel(
                    application.container.networkRepository,
                    context,
                    prefs.playerName,
                    prefs.gridSize
                ) as T
            }
        }
    )

    val state     by vm.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    // Navigation automatique quand la partie démarre
    LaunchedEffect(state.gameStarted, state.role) {
        if (state.gameStarted) {
            when (state.role) {
                "HOST"   -> onGameStartHost(state.selectedGridSize, state.localName, state.opponentName)
                "CLIENT" -> onGameStartClient(state.selectedGridSize, state.localName, state.opponentName)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("MULTIJOUEUR", color = OnSurface, fontWeight = FontWeight.Black)
                },
                navigationIcon = {
                    IconButton(onClick = { vm.stopHosting(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Primary)
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = SurfaceContainer) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(Modifier.size(8.dp).background(Primary, CircleShape))
                            Text("Wi-Fi Local", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Établissez un lien local pour affronter des adversaires sur le même réseau.",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant
            )

            // ── Erreur ───────────────────────────────────────────────────────
            AnimatedVisibility(state.error != null) {
                Surface(shape = RoundedCornerShape(12.dp), color = Error.copy(alpha = 0.15f)) {
                    Text(state.error ?: "", color = Error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp))
                }
            }

            // ── Pseudo commun ─────────────────────────────────────────────────
            OutlinedTextField(
                value = state.localName,
                onValueChange = { if (it.length <= 20) vm.updateLocalName(it) },
                label = { Text("Ton pseudo", color = OnSurfaceVariant) },
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

            // ── Taille de grille (hôte seulement) ────────────────────────────
            if (!state.isConnecting) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TAILLE DE GRILLE", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GridSize.entries.forEach { gs ->
                            val isSelected = state.selectedGridSize == gs
                            val borderColor = if (isSelected) Primary else OutlineVariant.copy(alpha = 0.4f)
                            val bgColor     = if (isSelected) Primary.copy(alpha = 0.12f) else SurfaceContainerHigh
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable(enabled = !state.isHosting) { vm.updateGridSize(gs) }
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
                }
            }

            // ── Carte Hôte ────────────────────────────────────────────────────
            HostCard(
                localIp         = state.localIp,
                isHosting       = state.isHosting,
                clientConnected = state.clientConnected,
                localName       = state.localName,
                opponentName    = state.opponentName,
                onCopyIp        = { clipboard.setText(AnnotatedString(state.localIp)) },
                onStartHosting  = vm::startHosting,
                onStop          = vm::stopHosting
            )

            // ── Carte Client ──────────────────────────────────────────────────
            ClientCard(
                inputIp      = state.inputIp,
                isConnecting = state.isConnecting,
                isHosting    = state.isHosting,
                onIpChange   = vm::updateInputIp,
                onJoin       = vm::joinGame
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HostCard(
    localIp: String,
    isHosting: Boolean,
    clientConnected: Boolean,
    localName: String,
    opponentName: String,
    onCopyIp: () -> Unit,
    onStartHosting: () -> Unit,
    onStop: () -> Unit
) {
    Surface(shape = RoundedCornerShape(20.dp), color = SurfaceContainerHigh) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("PROTOCOLE 01", style = MaterialTheme.typography.labelSmall, color = Primary)
                    Spacer(Modifier.height(4.dp))
                    Text("Héberger", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = OnSurface)
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Primary.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Router, null, tint = Primary,
                        modifier = Modifier.padding(12.dp).size(28.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info IP
            Surface(shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("IP locale :", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        TextButton(onClick = onCopyIp) {
                            Icon(Icons.Default.ContentCopy, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copier", color = Primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(localIp, style = MaterialTheme.typography.titleLarge,
                        color = Primary, fontWeight = FontWeight.Bold)
                    Text("Port : 5555", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Lobby", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            PlayerSlot(name = "${localName.ifBlank { "Toi" }} (X)", isHost = true)
            Spacer(Modifier.height(6.dp))
            PlayerSlot(
                name    = if (clientConnected) "${opponentName.ifBlank { "Adversaire" }} (O)" else "En attente...",
                isHost  = false,
                waiting = !clientConnected
            )

            Spacer(Modifier.height(16.dp))

            if (!isHosting) {
                Button(
                    onClick = onStartHosting,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(listOf(Primary, PrimaryContainer)), RoundedCornerShape(50)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Diffuser le signal", color = OnPrimaryContainer, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.5f))
                ) {
                    Text("Arrêter", color = Error)
                }
            }
        }
    }
}

@Composable
private fun PlayerSlot(name: String, isHost: Boolean, waiting: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isHost) SurfaceContainerHighest else SurfaceContainerLow.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isHost) Box(Modifier.size(4.dp).background(Primary, CircleShape))
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (waiting) OnSurfaceVariant else OnSurface,
                fontWeight = if (isHost) FontWeight.Bold else FontWeight.Normal
            )
            if (isHost) {
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(4.dp), color = Primary.copy(alpha = 0.1f)) {
                    Text("HÔTE", style = MaterialTheme.typography.labelSmall, color = Primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun ClientCard(
    inputIp: String,
    isConnecting: Boolean,
    isHosting: Boolean,
    onIpChange: (String) -> Unit,
    onJoin: () -> Unit
) {
    Surface(shape = RoundedCornerShape(20.dp), color = SurfaceContainerHigh) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("PROTOCOLE 02", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("Rejoindre", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = OnSurface)
                }
                Surface(shape = RoundedCornerShape(12.dp), color = SurfaceContainerHighest) {
                    Icon(Icons.Default.Sensors, null, tint = OnSurfaceVariant,
                        modifier = Modifier.padding(12.dp).size(24.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = inputIp,
                onValueChange = onIpChange,
                label = { Text("IP de l'hôte", color = OnSurfaceVariant) },
                placeholder = { Text("ex: 192.168.1.42", color = Outline) },
                singleLine = true,
                enabled = !isHosting && !isConnecting,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor      = Primary,
                    unfocusedTextColor    = Primary,
                    focusedBorderColor    = Primary,
                    unfocusedBorderColor  = OutlineVariant,
                    focusedContainerColor    = SurfaceContainerHighest,
                    unfocusedContainerColor  = SurfaceContainerHighest
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isHosting && !isConnecting,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHighest)
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Connexion...", color = OnSurface, fontWeight = FontWeight.Black)
                } else {
                    Text("Se connecter", color = OnSurface, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
