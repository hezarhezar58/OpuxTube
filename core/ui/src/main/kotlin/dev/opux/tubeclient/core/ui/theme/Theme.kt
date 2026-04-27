package dev.opux.tubeclient.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import dev.opux.tubeclient.core.domain.model.ThemeMode

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

private val OpuxLightColorScheme = lightColorScheme(
    primary = OpuxPrimary,
    onPrimary = OpuxOnPrimary,
    background = OpuxLightBackground,
    onBackground = OpuxLightOnSurface,
    surface = OpuxLightSurface,
    onSurface = OpuxLightOnSurface,
    surfaceVariant = OpuxLightSurfaceVariant,
    onSurfaceVariant = OpuxLightOnSurfaceVariant,
    outline = OpuxLightOutline,
    error = OpuxError,
)

@Composable
fun OpuxTubeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) OpuxDarkColorScheme else OpuxLightColorScheme,
        typography = OpuxTypography,
        content = content,
    )
}
