package dev.opux.tubeclient.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.opux.tubeclient.core.database.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId ORDER BY watchedAt DESC LIMIT 1")
    suspend fun findLastForVideo(videoId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WatchHistoryEntity)

    @Query("UPDATE watch_history SET progressMs = :progressMs, watchedAt = :watchedAt WHERE videoId = :videoId")
    suspend fun updateProgress(videoId: String, progressMs: Long, watchedAt: Long)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteByVideoId(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
