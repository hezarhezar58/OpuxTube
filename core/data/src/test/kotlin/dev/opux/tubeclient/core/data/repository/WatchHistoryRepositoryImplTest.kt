package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.database.dao.WatchHistoryDao
import dev.opux.tubeclient.core.database.entity.WatchHistoryEntity
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WatchHistoryRepositoryImplTest {

    @Test
    fun `observeRecent maps DAO rows to domain entries`() = runTest {
        val dao = mockk<WatchHistoryDao>()
        every { dao.observeRecent(any()) } returns flowOf(
            listOf(historyEntity(videoId = "a"), historyEntity(videoId = "b")),
        )

        val repo = WatchHistoryRepositoryImpl(dao)
        val items = repo.observeRecent(limit = 50).first()

        assertEquals(listOf("a", "b"), items.map { it.videoId })
    }

    @Test
    fun `record updates an existing row instead of inserting a duplicate`() = runTest {
        val dao = mockk<WatchHistoryDao>()
        coEvery { dao.findLastForVideo("vid") } returns historyEntity(videoId = "vid")
        coEvery { dao.updateProgress(any(), any(), any()) } returns Unit

        val repo = WatchHistoryRepositoryImpl(dao)
        repo.record(historyEntry(videoId = "vid", progressMs = 60_000L, watchedAt = 999L))

        coVerify { dao.updateProgress("vid", 60_000L, 999L) }
        coVerify(exactly = 0) { dao.upsert(any()) }
    }

    @Test
    fun `record inserts a new row when no entry exists for the video`() = runTest {
        val dao = mockk<WatchHistoryDao>()
        coEvery { dao.findLastForVideo("new") } returns null
        coEvery { dao.upsert(any()) } returns Unit

        val repo = WatchHistoryRepositoryImpl(dao)
        repo.record(historyEntry(videoId = "new"))

        coVerify { dao.upsert(match { it.videoId == "new" }) }
        coVerify(exactly = 0) { dao.updateProgress(any(), any(), any()) }
    }

    @Test
    fun `delete and clearAll forward verbatim`() = runTest {
        val dao = mockk<WatchHistoryDao>(relaxed = true)
        val repo = WatchHistoryRepositoryImpl(dao)

        repo.delete("v")
        repo.clearAll()

        coVerify { dao.deleteByVideoId("v") }
        coVerify { dao.clearAll() }
    }

    private fun historyEntity(videoId: String): WatchHistoryEntity = WatchHistoryEntity(
        id = 0,
        videoId = videoId,
        videoUrl = "https://example.com/watch?v=$videoId",
        title = "Title $videoId",
        channelName = "Channel",
        channelUrl = "https://example.com/ch",
        thumbnailUrl = null,
        durationSeconds = 200,
        progressMs = 0,
        watchedAt = 1000L,
    )

    private fun historyEntry(
        videoId: String,
        progressMs: Long = 0L,
        watchedAt: Long = 1000L,
    ): WatchHistoryEntry = WatchHistoryEntry(
        videoId = videoId,
        videoUrl = "https://example.com/watch?v=$videoId",
        title = "Title $videoId",
        channelName = "Channel",
        channelUrl = "https://example.com/ch",
        thumbnailUrl = null,
        durationSeconds = 200,
        progressMs = progressMs,
        watchedAt = watchedAt,
    )
}
