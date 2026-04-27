package dev.opux.tubeclient.core.ui.util

import android.content.Context
import dev.opux.tubeclient.core.ui.R
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
 * Converts an upload-date string into a localized relative phrase ("3 days ago"
 * / "3 gün önce"). Handles ISO 8601 timestamps from NewPipe's StreamInfo path;
 * also normalizes English "N units ago" strings into the active locale before
 * returning the input unchanged as a last resort.
 */
fun String?.formatRelativeUploadDate(
    context: Context,
    now: Instant = Instant.now(),
): String? {
    if (this.isNullOrBlank()) return null
    val instant = parseIsoOrNull()
    if (instant != null) {
        return formatRelativeFromInstant(context, instant, now)
    }
    return englishRelativeToLocalized(context, this) ?: this
}

fun Long.formatRelativeMillis(
    context: Context,
    now: Instant = Instant.now(),
): String = formatRelativeFromInstant(context, Instant.ofEpochMilli(this), now)

private fun formatRelativeFromInstant(
    context: Context,
    instant: Instant,
    now: Instant,
): String {
    val seconds = (now.epochSecond - instant.epochSecond).coerceAtLeast(0)
    return when {
        seconds < 60 -> context.getString(R.string.core_just_now)
        seconds < 3600 -> context.getString(R.string.core_minutes_ago, (seconds / 60).toInt())
        seconds < 86_400 -> context.getString(R.string.core_hours_ago, (seconds / 3600).toInt())
        seconds < 604_800 -> context.getString(R.string.core_days_ago, (seconds / 86_400).toInt())
        seconds < 2_592_000 -> context.getString(R.string.core_weeks_ago, (seconds / 604_800).toInt())
        seconds < 31_536_000 -> context.getString(R.string.core_months_ago, (seconds / 2_592_000).toInt())
        else -> context.getString(R.string.core_years_ago, (seconds / 31_536_000).toInt())
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

private fun englishRelativeToLocalized(context: Context, text: String): String? {
    val match = ENGLISH_AGO_REGEX.find(text) ?: return null
    val value = match.groupValues[1].toIntOrNull() ?: return null
    val resId = when (match.groupValues[2].lowercase(Locale.ROOT)) {
        "second", "minute" -> R.string.core_minutes_ago
        "hour" -> R.string.core_hours_ago
        "day" -> R.string.core_days_ago
        "week" -> R.string.core_weeks_ago
        "month" -> R.string.core_months_ago
        "year" -> R.string.core_years_ago
        else -> return null
    }
    return context.getString(resId, value)
}
