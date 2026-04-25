package dev.opux.tubeclient.core.ui.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Locale

fun Long.formatDuration(): String {
    if (this <= 0L) return "--:--"
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}

fun Long.formatViewCount(): String {
    if (this < 0L) return "—"
    return when {
        this >= 1_000_000_000L -> formatCompact(this / 1_000_000_000.0, "Mr")
        this >= 1_000_000L -> formatCompact(this / 1_000_000.0, "Mn")
        this >= 1_000L -> formatCompact(this / 1_000.0, "B")
        else -> this.toString()
    }
}

private fun formatCompact(value: Double, suffix: String): String {
    val formatted = String.format(Locale.ROOT, "%.1f", value)
    val trimmed = if (formatted.endsWith(".0")) formatted.dropLast(2) else formatted
    return "$trimmed $suffix"
}

/**
 * Converts an upload-date string into a Turkish relative phrase ("3 gün önce").
 * Handles ISO 8601 timestamps from NewPipe's StreamInfo path; falls back to the input
 * unchanged when it's already a localized string ("7 days ago" / "1 hafta önce") or null.
 */
fun String?.formatRelativeUploadDate(now: Instant = Instant.now()): String? {
    if (this.isNullOrBlank()) return null
    val instant = parseIsoOrNull()
    if (instant != null) {
        return formatRelativeFromInstant(instant, now)
    }
    return englishRelativeToTurkish(this) ?: this
}

fun Long.formatRelativeMillis(now: Instant = Instant.now()): String =
    formatRelativeFromInstant(Instant.ofEpochMilli(this), now)

private fun formatRelativeFromInstant(instant: Instant, now: Instant): String {
    val seconds = (now.epochSecond - instant.epochSecond).coerceAtLeast(0)
    return when {
        seconds < 60 -> "az önce"
        seconds < 3600 -> "${seconds / 60} dakika önce"
        seconds < 86_400 -> "${seconds / 3600} saat önce"
        seconds < 604_800 -> "${seconds / 86_400} gün önce"
        seconds < 2_592_000 -> "${seconds / 604_800} hafta önce"
        seconds < 31_536_000 -> "${seconds / 2_592_000} ay önce"
        else -> "${seconds / 31_536_000} yıl önce"
    }
}

private fun String.parseIsoOrNull(): Instant? {
    if (!contains('T') || length < 10) return null
    return try {
        OffsetDateTime.parse(this).toInstant()
    } catch (_: DateTimeParseException) {
        try { Instant.parse(this) } catch (_: DateTimeParseException) { null }
    }
}

private val ENGLISH_AGO_REGEX = Regex(
    """(\d+)\s+(second|minute|hour|day|week|month|year)s?\s+ago""",
    RegexOption.IGNORE_CASE,
)

private val ENGLISH_UNIT_TO_TURKISH = mapOf(
    "second" to "saniye",
    "minute" to "dakika",
    "hour" to "saat",
    "day" to "gün",
    "week" to "hafta",
    "month" to "ay",
    "year" to "yıl",
)

private fun englishRelativeToTurkish(text: String): String? {
    val match = ENGLISH_AGO_REGEX.find(text) ?: return null
    val unit = ENGLISH_UNIT_TO_TURKISH[match.groupValues[2].lowercase(Locale.ROOT)] ?: return null
    return "${match.groupValues[1]} $unit önce"
}
