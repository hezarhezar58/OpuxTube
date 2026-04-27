package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.PlaylistEntry
import dev.opux.tubeclient.core.domain.model.VideoDetail
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>
    fun observeEntries(playlistId: Long): Flow<List<PlaylistEntry>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addVideo(playlistId: Long, video: VideoDetail)
    suspend fun removeVideo(playlistId: Long, videoId: String)
}
