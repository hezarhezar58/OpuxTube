package dev.opux.tubeclient.core.domain.preferences

import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import kotlinx.coroutines.flow.Flow

interface SponsorBlockPreferences {
    val enabledCategories: Flow<Set<SponsorBlockCategory>>
    suspend fun setCategoryEnabled(category: SponsorBlockCategory, enabled: Boolean)

    companion object {
        val DEFAULT_ENABLED: Set<SponsorBlockCategory> = SponsorBlockCategory.entries.toSet()
    }
}
