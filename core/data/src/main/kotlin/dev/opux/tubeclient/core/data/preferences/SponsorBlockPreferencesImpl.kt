package dev.opux.tubeclient.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import dev.opux.tubeclient.core.domain.preferences.SponsorBlockPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sponsorBlockDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sponsorblock_prefs",
)

@Singleton
class SponsorBlockPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SponsorBlockPreferences {

    private val store: DataStore<Preferences> = context.sponsorBlockDataStore

    override val enabledCategories: Flow<Set<SponsorBlockCategory>> =
        store.data.map { prefs ->
            val raw: Set<String>? = prefs[ENABLED_KEY]
            if (raw == null) {
                SponsorBlockPreferences.DEFAULT_ENABLED
            } else {
                raw.mapNotNull(SponsorBlockCategory::fromApiKey).toSet()
            }
        }

    override suspend fun setCategoryEnabled(category: SponsorBlockCategory, enabled: Boolean) {
        store.edit { prefs ->
            val current: Set<String> = prefs[ENABLED_KEY]
                ?: SponsorBlockPreferences.DEFAULT_ENABLED.map { it.apiKey }.toSet()
            prefs[ENABLED_KEY] = if (enabled) current + category.apiKey else current - category.apiKey
        }
    }

    private companion object {
        val ENABLED_KEY = stringSetPreferencesKey("enabled_categories")
    }
}
