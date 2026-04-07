package com.gridclash.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GridClashColorScheme = darkColorScheme(
    primary              = Primary,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = OnPrimaryContainer,
    secondary            = Secondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary             = Tertiary,
    tertiaryContainer    = TertiaryContainer,
    onTertiary           = OnTertiary,
    background           = Background,
    onBackground         = OnSurface,
    surface              = Surface,
    onSurface            = OnSurface,
    onSurfaceVariant     = OnSurfaceVariant,
    surfaceVariant       = SurfaceContainerHighest,
    outline              = Outline,
    outlineVariant       = OutlineVariant,
    error                = Error,
    onError              = OnError
)

@Composable
fun GridClashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GridClashColorScheme,
        typography  = GridClashTypography,
        content     = content
    )
}
