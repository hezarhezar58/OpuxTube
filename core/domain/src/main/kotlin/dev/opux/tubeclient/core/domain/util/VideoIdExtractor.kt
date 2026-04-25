package dev.opux.tubeclient.core.domain.util

object VideoIdExtractor {

    private val patterns = listOf(
        Regex("""youtube\.com/watch\?.*?v=([A-Za-z0-9_-]{11})"""),
        Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/embed/([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/shorts/([A-Za-z0-9_-]{11})"""),
    )

    fun extractId(url: String): String? {
        if (url.length == 11 && url.matches(Regex("[A-Za-z0-9_-]{11}"))) return url
        for (regex in patterns) {
            regex.find(url)?.groupValues?.getOrNull(1)?.let { return it }
        }
        return null
    }

    fun buildWatchUrl(videoId: String): String = "https://www.youtube.com/watch?v=$videoId"
}
