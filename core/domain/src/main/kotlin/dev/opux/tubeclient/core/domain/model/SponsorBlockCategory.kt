package dev.opux.tubeclient.core.domain.model

enum class SponsorBlockCategory(val apiKey: String) {
    SPONSOR("sponsor"),
    INTRO("intro"),
    OUTRO("outro"),
    SELF_PROMO("selfpromo"),
    INTERACTION("interaction"),
    MUSIC_OFFTOPIC("music_offtopic");

    companion object {
        fun fromApiKey(key: String): SponsorBlockCategory? =
            entries.firstOrNull { it.apiKey == key }
    }
}
