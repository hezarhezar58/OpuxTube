package dev.opux.tubeclient.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OpuxDarkColorScheme = darkColorScheme(
    primary = OpuxPrimary,
    onPrimary = OpuxOnPrimary,
    background = OpuxBlack,
    onBackground = OpuxOnSurface,
    surface = OpuxSurface,
    onSurface = OpuxOnSurface,
    surfaceVariant = OpuxSurfaceVariant,
    onSurfaceVariant = OpuxOnSurfaceVariant,
    outline = OpuxOutline,
    error = OpuxError,
)

@Composable
fun OpuxTubeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OpuxDarkColorScheme,
        typography = OpuxTypography,
        content = content,
    )
}
