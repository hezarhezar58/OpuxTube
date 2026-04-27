package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.database.dao.PlaylistDao
import dev.opux.tubeclient.core.database.dao.PlaylistEntryCount
import dev.opux.tubeclient.core.database.entity.PlaylistEntity
import dev.opux.tubeclient.core.database.entity.PlaylistEntryEntity
import dev.opux.tubeclient.core.domain.model.AudioStream
import dev.opux.tubeclient.core.domain.model.ChannelSummary
import dev.opux.tubeclient.core.domain.model.VideoDetail
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistRepositoryImplTest {

    @Test
    fun `observePlaylists merges entry counts onto each playlist`() = runTest {
        val dao = mockk<PlaylistDao>()
        every { dao.observePlaylists() } returns flowOf(
            listOf(
                PlaylistEntity(id = 1, name = "Watch Later", createdAt = 100, updatedAt = 200),
                PlaylistEntity(id = 2, name = "Empty", createdAt = 50, updatedAt = 60),
            ),
        )
        every { dao.observeEntryCounts() } returns flowOf(
            listOf(PlaylistEntryCount(playlistId = 1, count = 3)),
        )

        val repo = PlaylistRepositoryImpl(dao)
        val playlists = repo.observePlaylists().first()

        assertEquals(2, playlists.size)
        assertEquals(3, playlists.first { it.id == 1L }.itemCount)
        assertEquals(0, playlists.first { it.id == 2L }.itemCount)
    }

    @Test
    fun `createPlaylist forwards trimmed name with current timestamp and returns the id`() = runTest {
        val dao = mockk<PlaylistDao>()
        val captured = slot<PlaylistEntity>()
        coEvery { dao.insertPlaylist(capture(captured)) } returns 42L

        val repo = PlaylistRepositoryImpl(dao)
        val id = repo.createPlaylist("My playlist")

        assertEquals(42L, id)
        assertEquals("My playlist", captured.captured.name)
        // Timestamps should match each other (createdAt == updatedAt at creation).
        assertEquals(captured.captured.createdAt, captured.captured.updatedAt)
    }

    @Test
    fun `addVideo inserts at maxPosition+1 and bumps updatedAt`() = runTest {
        val dao = mockk<PlaylistDao>()
        val entry = slot<PlaylistEntryEntity>()
        coEvery { dao.maxPosition(7L) } returns 4
        coEvery { dao.addVideo(any(), capture(entry)) } returns Unit
        coEvery { dao.touchPlaylist(any(), any()) } returns Unit

        val repo = PlaylistRepositoryImpl(dao)
        repo.addVideo(7L, sampleDetail())

        assertEquals(7L, entry.captured.playlistId)
        assertEquals("vid-id", entry.captured.videoId)
        assertEquals(5, entry.captured.position)
        coVerify { dao.touchPlaylist(7L, any()) }
    }

    @Test
    fun `removeVideo touches the playlist so it floats to the top of the list`() = runTest {
        val dao = mockk<PlaylistDao>(relaxed = true)
        val repo = PlaylistRepositoryImpl(dao)

        repo.removeVideo(7L, "vid-id")

        coVerify { dao.removeVideo(7L, "vid-id") }
        coVerify { dao.touchPlaylist(7L, any()) }
    }

    @Test
    fun `deletePlaylist forwards to the dao verbatim`() = runTest {
        val dao = mockk<PlaylistDao>(relaxed = true)
        val repo = PlaylistRepositoryImpl(dao)

        repo.deletePlaylist(11L)

        coVerify { dao.deletePlaylist(11L) }
    }

    private fun sampleDetail(): VideoDetail = VideoDetail(
        id = "vid-id",
        url = "https://example.com/watch?v=vid-id",
        title = "Some video",
        description = "",
        thumbnailUrl = "https://example.com/thumb.jpg",
        durationSeconds = 240,
        viewCount = 100,
        likeCount = 10,
        uploadedAt = null,
        category = null,
        channel = ChannelSummary(
            name = "Channel",
            url = "https://example.com/ch",
            avatarUrl = null,
            subscriberCount = 0,
            isVerified = false,
        ),
        videoStreams = emptyList(),
        audioStreams = emptyList<AudioStream>(),
        videoOnlyStreams = emptyList(),
        relatedVideos = emptyList(),
        isLive = false,
    )
}
