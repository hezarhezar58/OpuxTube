package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.database.dao.DownloadedVideoDao
import dev.opux.tubeclient.core.database.entity.DownloadedVideoEntity
import dev.opux.tubeclient.core.domain.model.DownloadedVideo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DownloadRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `observeAll maps DAO rows to domain DownloadedVideo objects`() = runTest {
        val dao = mockk<DownloadedVideoDao>()
        every { dao.observeAll() } returns flowOf(
            listOf(
                downloadedEntity(id = "v1"),
                downloadedEntity(id = "v2"),
            ),
        )

        val repo = DownloadRepositoryImpl(dao)
        val items = repo.observeAll().first()

        assertEquals(2, items.size)
        assertEquals(listOf("v1", "v2"), items.map { it.videoId })
    }

    @Test
    fun `delete removes the on-disk file before deleting the DB row`() = runTest {
        val file = tempFolder.newFile("vid.mp4").apply { writeText("payload") }
        val dao = mockk<DownloadedVideoDao>()
        coEvery { dao.findByVideoId("vid") } returns downloadedEntity(id = "vid", path = file.absolutePath)
        coEvery { dao.delete("vid") } returns Unit

        val repo = DownloadRepositoryImpl(dao)
        assertTrue("File should exist before delete", file.exists())

        repo.delete("vid")

        assertFalse("File should be gone after delete", file.exists())
        coVerify { dao.delete("vid") }
    }

    @Test
    fun `clearAll deletes every file from disk and then clears the DB`() = runTest {
        val f1 = tempFolder.newFile("a.mp4").apply { writeText("a") }
        val f2 = tempFolder.newFile("b.mp4").apply { writeText("b") }
        val dao = mockk<DownloadedVideoDao>()
        every { dao.observeAll() } returns flowOf(
            listOf(
                downloadedEntity(id = "a", path = f1.absolutePath),
                downloadedEntity(id = "b", path = f2.absolutePath),
            ),
        )
        coEvery { dao.clearAll() } returns Unit

        val repo = DownloadRepositoryImpl(dao)
        repo.clearAll()

        assertFalse(f1.exists())
        assertFalse(f2.exists())
        coVerify { dao.clearAll() }
    }

    @Test
    fun `upsert maps domain back to entity and forwards`() = runTest {
        val dao = mockk<DownloadedVideoDao>(relaxed = true)
        val repo = DownloadRepositoryImpl(dao)

        repo.upsert(
            DownloadedVideo(
                videoId = "vid",
                videoUrl = "https://example.com/watch?v=vid",
                title = "t",
                channelName = "ch",
                thumbnailUrl = null,
                durationSeconds = 100,
                filePath = "/tmp/vid.mp4",
                fileSizeBytes = 12345L,
                mimeType = "video/mp4",
                isAudioOnly = false,
                downloadedAt = 999L,
            ),
        )

        coVerify {
            dao.upsert(
                match { it.videoId == "vid" && it.fileSizeBytes == 12345L && it.downloadedAt == 999L },
            )
        }
    }

    private fun downloadedEntity(
        id: String,
        path: String = "/tmp/$id.mp4",
    ): DownloadedVideoEntity = DownloadedVideoEntity(
        videoId = id,
        videoUrl = "https://example.com/watch?v=$id",
        title = "Video $id",
        channelName = "Channel",
        thumbnailUrl = null,
        durationSeconds = 100,
        filePath = path,
        fileSizeBytes = File(path).takeIf { it.exists() }?.length() ?: 0L,
        mimeType = "video/mp4",
        isAudioOnly = false,
        downloadedAt = System.currentTimeMillis(),
    )
}
