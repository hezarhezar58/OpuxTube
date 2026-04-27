package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.database.dao.SearchHistoryDao
import dev.opux.tubeclient.core.database.entity.SearchHistoryEntity
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

class SearchHistoryRepositoryImplTest {

    @Test
    fun `observeRecent maps DAO entries to plain query strings preserving order`() = runTest {
        val dao = mockk<SearchHistoryDao>()
        every { dao.observeRecent(any()) } returns flowOf(
            listOf(
                SearchHistoryEntity(query = "lofi", lastUsedAt = 200),
                SearchHistoryEntity(query = "blok3", lastUsedAt = 100),
            ),
        )

        val repo = SearchHistoryRepositoryImpl(dao)
        val items = repo.observeRecent().first()

        assertEquals(listOf("lofi", "blok3"), items)
    }

    @Test
    fun `record skips blank queries entirely`() = runTest {
        val dao = mockk<SearchHistoryDao>(relaxed = true)
        val repo = SearchHistoryRepositoryImpl(dao)

        repo.record("")
        repo.record("   ")

        coVerify(exactly = 0) { dao.upsert(any()) }
    }

    @Test
    fun `record upserts a SearchHistoryEntity tagged with the current time`() = runTest {
        val dao = mockk<SearchHistoryDao>()
        val captured = slot<SearchHistoryEntity>()
        coEvery { dao.upsert(capture(captured)) } returns Unit

        val before = System.currentTimeMillis()
        val repo = SearchHistoryRepositoryImpl(dao)
        repo.record("blok3")
        val after = System.currentTimeMillis()

        assertEquals("blok3", captured.captured.query)
        assertTrue(
            "lastUsedAt should be near now: was ${captured.captured.lastUsedAt}",
            captured.captured.lastUsedAt in before..after,
        )
    }

    @Test
    fun `delete and clearAll forward to the DAO`() = runTest {
        val dao = mockk<SearchHistoryDao>(relaxed = true)
        val repo = SearchHistoryRepositoryImpl(dao)

        repo.delete("blok3")
        repo.clearAll()

        coVerify { dao.delete("blok3") }
        coVerify { dao.clearAll() }
    }
}
