package dev.opux.tubeclient.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.opux.tubeclient.R
import dev.opux.tubeclient.core.domain.model.DownloadStatus
import dev.opux.tubeclient.core.domain.model.DownloadedVideo
import dev.opux.tubeclient.core.domain.repository.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class OpuxDownloadService : Service() {

    @Inject lateinit var coordinator: DownloadCoordinator
    @Inject lateinit var okHttpClient: OkHttpClient
    @Inject lateinit var downloadRepository: DownloadRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var workerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(text = "İndirme hazırlanıyor…"))
        if (workerJob?.isActive != true) {
            workerJob = scope.launch { runQueue() }
        }
        return START_NOT_STICKY
    }

    private suspend fun runQueue() {
        while (true) {
            val job = coordinator.pollNext() ?: break
            updateNotification("İndiriliyor: ${job.title}")
            coordinator.setStatus(job.videoId, DownloadStatus.InProgress(0L, 0L))
            val result = runCatching { downloadOne(job) }
            result
                .onSuccess { sizeBytes ->
                    downloadRepository.upsert(
                        DownloadedVideo(
                            videoId = job.videoId,
                            videoUrl = job.videoUrl,
                            title = job.title,
                            channelName = job.channelName,
                            thumbnailUrl = job.thumbnailUrl,
                            durationSeconds = job.durationSeconds,
                            filePath = job.targetFilePath,
                            fileSizeBytes = sizeBytes,
                            mimeType = job.mimeType,
                            isAudioOnly = false,
                            downloadedAt = System.currentTimeMillis(),
                        ),
                    )
                    coordinator.setStatus(job.videoId, DownloadStatus.Completed)
                }
                .onFailure { t ->
                    Timber.w(t, "Download failed for %s", job.videoId)
                    runCatching { File(job.targetFilePath).delete() }
                    coordinator.setStatus(
                        job.videoId,
                        DownloadStatus.Failed(t.message ?: "İndirme başarısız"),
                    )
                }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun downloadOne(job: DownloadJob): Long {
        val request = Request.Builder().url(job.sourceUrl).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            val body = response.body ?: throw IOException("Empty body")
            val total = body.contentLength().coerceAtLeast(0L)
            val tempFile = File(job.targetFilePath + ".part")
            tempFile.parentFile?.mkdirs()
            tempFile.sink().buffer().use { sink ->
                body.source().use { source ->
                    val bufferSize = 64L * 1024L
                    var totalRead = 0L
                    var lastReportedAt = 0L
                    while (true) {
                        val read = source.read(sink.buffer, bufferSize)
                        if (read == -1L) break
                        sink.emitCompleteSegments()
                        totalRead += read
                        val now = System.currentTimeMillis()
                        if (now - lastReportedAt > 250) {
                            coordinator.setStatus(
                                job.videoId,
                                DownloadStatus.InProgress(totalRead, total),
                            )
                            updateNotification(
                                if (total > 0) {
                                    val pct = (totalRead * 100L / total).toInt()
                                    "%${pct} · ${job.title}"
                                } else {
                                    "İndiriliyor: ${job.title}"
                                },
                            )
                            lastReportedAt = now
                        }
                    }
                    sink.flush()
                }
            }
            val finalFile = File(job.targetFilePath)
            if (finalFile.exists()) finalFile.delete()
            if (!tempFile.renameTo(finalFile)) {
                throw IOException("Rename failed")
            }
            return finalFile.length()
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "İndirmeler",
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("OpuxTube")
        .setContentText(text)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .build()

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val CHANNEL_ID = "opux_downloads"
        const val NOTIFICATION_ID = 4242
    }
}
