package dev.opux.tubeclient.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.opux.tubeclient.core.domain.model.ThemeMode
import dev.opux.tubeclient.core.domain.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_prefs",
)

@Singleton
class AppPreferencesImpl @Inject constructor(
    @ApplicationContext context: Context,
) : AppPreferences {

    private val store: DataStore<Preferences> = context.appDataStore

    override val themeMode: Flow<ThemeMode> = store.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[THEME_KEY] = mode.name }
    }

    private companion object {
        val THEME_KEY = stringPreferencesKey("theme_mode")
    }
}
