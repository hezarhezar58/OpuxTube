package dev.opux.tubeclient.core.domain.model

data class SkipSegment(
    val uuid: String,
    val category: String,
    val startMs: Long,
    val endMs: Long,
) {
    val durationMs: Long get() = (endMs - startMs).coerceAtLeast(0L)
}
