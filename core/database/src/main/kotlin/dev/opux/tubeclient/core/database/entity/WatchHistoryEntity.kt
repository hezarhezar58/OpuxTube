package dev.opux.tubeclient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: String,
    val videoUrl: String,
    val title: String,
    val channelName: String,
    val channelUrl: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val progressMs: Long,
    val watchedAt: Long,
)
