package dev.opux.tubeclient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_videos")
data class DownloadedVideoEntity(
    @PrimaryKey val videoId: String,
    val videoUrl: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val filePath: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val isAudioOnly: Boolean,
    val downloadedAt: Long,
)
