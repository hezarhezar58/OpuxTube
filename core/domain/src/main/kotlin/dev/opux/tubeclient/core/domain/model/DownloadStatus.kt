package dev.opux.tubeclient.core.domain.model

sealed interface DownloadStatus {
    data object Queued : DownloadStatus
    data class InProgress(val bytesDownloaded: Long, val totalBytes: Long) : DownloadStatus {
        val progress: Float get() = if (totalBytes > 0) {
            (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }
    data object Completed : DownloadStatus
    data class Failed(val message: String) : DownloadStatus
}
