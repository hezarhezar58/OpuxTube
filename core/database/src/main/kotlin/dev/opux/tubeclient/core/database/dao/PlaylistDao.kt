package dev.opux.tubeclient.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.opux.tubeclient.core.database.entity.PlaylistEntity
import dev.opux.tubeclient.core.database.entity.PlaylistEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT playlistId, COUNT(*) AS count FROM playlist_entries GROUP BY playlistId")
    fun observeEntryCounts(): Flow<List<PlaylistEntryCount>>

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_entries WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int

    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun touchPlaylist(playlistId: Long, timestamp: Long)

    @Query("SELECT * FROM playlist_entries WHERE playlistId = :playlistId ORDER BY position")
    fun observeEntries(playlistId: Long): Flow<List<PlaylistEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PlaylistEntryEntity)

    @Transaction
    suspend fun addVideo(playlistId: Long, entry: PlaylistEntryEntity) {
        insertEntry(entry.copy(playlistId = playlistId))
    }

    @Query("DELETE FROM playlist_entries WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideo(playlistId: Long, videoId: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)
}

data class PlaylistEntryCount(
    val playlistId: Long,
    val count: Int,
)
