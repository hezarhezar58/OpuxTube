package dev.opux.tubeclient.download

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.opux.tubeclient.core.domain.model.DownloadStatus
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoStream
import dev.opux.tubeclient.core.domain.repository.DownloadActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadJob(
    val videoId: String,
    val sourceUrl: String,
    val targetFilePath: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val mimeType: String,
    val videoUrl: String,
)

@Singleton
class DownloadCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
) : DownloadActions {
    private val _statuses = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    override val statuses: StateFlow<Map<String, DownloadStatus>> = _statuses.asStateFlow()

    private val pendingJobs = ArrayDeque<DownloadJob>()
    private val lock = Any()

    override fun enqueue(detail: VideoDetail, stream: VideoStream): Boolean {
        val current = _statuses.value[detail.id]
        if (current is DownloadStatus.InProgress || current is DownloadStatus.Queued) return false
        val targetDir = File(context.filesDir, "downloads").apply { mkdirs() }
        val target = File(targetDir, "${detail.id}.mp4")
        val job = DownloadJob(
            videoId = detail.id,
            sourceUrl = stream.url,
            targetFilePath = target.absolutePath,
            title = detail.title,
            channelName = detail.channel.name,
            thumbnailUrl = detail.thumbnailUrl,
            durationSeconds = detail.durationSeconds,
            mimeType = stream.mimeType ?: "video/mp4",
            videoUrl = detail.url,
        )
        synchronized(lock) {
            pendingJobs.addLast(job)
            _statuses.update { it + (detail.id to DownloadStatus.Queued) }
        }
        startServiceIfNeeded()
        return true
    }

    internal fun pollNext(): DownloadJob? = synchronized(lock) {
        pendingJobs.removeFirstOrNull()
    }

    internal fun setStatus(videoId: String, status: DownloadStatus) {
        _statuses.update { it + (videoId to status) }
    }

    private fun startServiceIfNeeded() {
        val intent = Intent(context, OpuxDownloadService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }
}
