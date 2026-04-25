package dev.opux.tubeclient.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.opux.tubeclient.core.database.entity.DownloadedVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedVideoDao {

    @Query("SELECT * FROM downloaded_videos ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadedVideoEntity>>

    @Query("SELECT * FROM downloaded_videos WHERE videoId = :videoId LIMIT 1")
    suspend fun findByVideoId(videoId: String): DownloadedVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadedVideoEntity)

    @Query("DELETE FROM downloaded_videos WHERE videoId = :videoId")
    suspend fun delete(videoId: String)
}
