package com.gridclash.app.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gridclash.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Règles", color = OnSurface, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Primary)
                    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text("GUIDE INSTRUCTIONNEL",
                    style = MaterialTheme.typography.labelSmall, color = Primary)
                Spacer(Modifier.height(4.dp))
                Text("Maîtriser\nla Neon Arena",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = OnSurface,
                    lineHeight = MaterialTheme.typography.headlineLarge.fontSize * 1.2
                )
            }

            RuleCard(
                emoji = "🎯",
                title = "Objectif",
                body  = "Aligne 3 de tes symboles en ligne, colonne ou diagonale sur la grille 3×3 avant ton adversaire.",
                borderColor = Primary
            )
            RuleCard(
                emoji = "🔄",
                title = "Déroulement",
                body  = "Les joueurs jouent en alternance. X commence toujours. Un seul symbole par case, par tour.",
                borderColor = Tertiary
            )
            RuleCard(
                emoji = "🤖",
                title = "Modes de difficulté",
                body  = "• Facile — l'IA joue aléatoirement.\n• Moyen — l'IA bloque et attaque stratégiquement.\n• Difficile — l'IA utilise Minimax : elle est imbattable.",
                borderColor = Error
            )
            RuleCard(
                emoji = "📡",
                title = "Multijoueur local",
                body  = "Les deux appareils doivent être sur le même réseau Wi-Fi. L'hôte affiche son IP, le client la saisit pour rejoindre.",
                borderColor = Primary
            )

            // Astuce finale
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceBright
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚡ PRO TIPS", style = MaterialTheme.typography.labelSmall, color = Primary)
                    TipRow("01", "Contrôle le centre (case 4) dès le premier coup.")
                    TipRow("02", "Prends les coins pour maximiser tes combinaisons.")
                    TipRow("03", "Bloque toujours une double menace adversaire.")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "BONNE CHANCE, PILOTE.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RuleCard(emoji: String, title: String, body: String, borderColor: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = borderColor.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
                Text(title, style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = OnSurface)
            }
            Text(body, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.6)
        }
    }
}

@Composable
private fun TipRow(number: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(number, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black, color = Primary)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant,
            modifier = Modifier.weight(1f))
    }
}
