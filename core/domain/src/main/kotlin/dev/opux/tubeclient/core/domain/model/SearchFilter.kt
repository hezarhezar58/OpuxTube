package dev.opux.tubeclient.core.domain.model

enum class SearchFilter {
    ALL,
    VIDEOS,
    CHANNELS,
    PLAYLISTS,
}

enum class StreamQuality(val label: String, val height: Int) {
    AUTO("Otomatik", 0),
    P144("144p", 144),
    P240("240p", 240),
    P360("360p", 360),
    P480("480p", 480),
    P720("720p", 720),
    P1080("1080p", 1080),
    P1440("1440p", 1440),
    P2160("2160p", 2160);

    companion object {
        fun fromHeight(h: Int): StreamQuality =
            entries.lastOrNull { it.height <= h && it.height > 0 } ?: AUTO
    }
}
