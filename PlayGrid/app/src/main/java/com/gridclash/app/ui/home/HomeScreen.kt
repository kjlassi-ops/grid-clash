package com.gridclash.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gridclash.app.ui.theme.*

@Composable
fun HomeScreen(
    onSoloClick: () -> Unit,
    onMultiplayerClick: () -> Unit,
    onRulesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Fond : grille de lignes cyan très subtiles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step  = 40.dp.toPx()
            val color = Color(0xFF00F5FF).copy(alpha = 0.04f)
            var x = 0f
            while (x <= size.width) {
                drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Badge saison
            Surface(
                shape = CircleShape,
                color = SurfaceContainerHigh,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "SEASON 01  •  ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Titre
            Text(
                text       = "GRID",
                style      = MaterialTheme.typography.displayLarge,
                color      = OnSurface,
                fontWeight = FontWeight.Black
            )
            Text(
                text       = "CLASH",
                style      = MaterialTheme.typography.displayLarge,
                color      = Primary,
                fontWeight = FontWeight.Black,
                modifier   = Modifier.offset(y = (-16).dp)
            )

            Text(
                text      = "Domine la grille. Bats tes adversaires.",
                style     = MaterialTheme.typography.bodyLarge,
                color     = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .padding(top = 8.dp, bottom = 48.dp)
                    .widthIn(max = 280.dp)
            )

            // CTA principal : Play Solo
            Button(
                onClick        = onSoloClick,
                modifier       = Modifier.fillMaxWidth().height(60.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape          = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(Primary, PrimaryContainer)),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = OnPrimaryContainer)
                        Text(
                            "Jouer Solo",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = OnPrimaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Multijoueur
            OutlinedButton(
                onClick  = onMultiplayerClick,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerHighest),
                border   = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.People, null, tint = Primary)
                        Text("Multijoueur Local", style = MaterialTheme.typography.titleLarge, color = OnSurface)
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Primary.copy(alpha = 0.12f)) {
                        Text(
                            "Wi-Fi",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ligne secondaire
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryActionButton(
                    text     = "Règles",
                    icon     = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f),
                    onClick  = onRulesClick
                )
                SecondaryActionButton(
                    text     = "Paramètres",
                    icon     = Icons.Default.Settings,
                    modifier = Modifier.weight(1f),
                    onClick  = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(52.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfaceContainerLow.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
    ) {
        Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}
