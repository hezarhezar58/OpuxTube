package dev.opux.tubeclient.core.domain.preferences

import dev.opux.tubeclient.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppPreferences {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
