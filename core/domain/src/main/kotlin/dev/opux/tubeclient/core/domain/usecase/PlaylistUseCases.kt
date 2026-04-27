package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.PlaylistEntry
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class ObservePlaylistsUseCase(private val repo: PlaylistRepository) {
    operator fun invoke(): Flow<List<Playlist>> = repo.observePlaylists()
}

class ObservePlaylistEntriesUseCase(private val repo: PlaylistRepository) {
    operator fun invoke(playlistId: Long): Flow<List<PlaylistEntry>> = repo.observeEntries(playlistId)
}

class CreatePlaylistUseCase(private val repo: PlaylistRepository) {
    suspend operator fun invoke(name: String): Long = repo.createPlaylist(name.trim())
}

class DeletePlaylistUseCase(private val repo: PlaylistRepository) {
    suspend operator fun invoke(playlistId: Long) = repo.deletePlaylist(playlistId)
}

class AddVideoToPlaylistUseCase(private val repo: PlaylistRepository) {
    suspend operator fun invoke(playlistId: Long, video: VideoDetail) =
        repo.addVideo(playlistId, video)
}

class RemoveVideoFromPlaylistUseCase(private val repo: PlaylistRepository) {
    suspend operator fun invoke(playlistId: Long, videoId: String) =
        repo.removeVideo(playlistId, videoId)
}
