package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import dev.opux.tubeclient.core.domain.preferences.SponsorBlockPreferences
import dev.opux.tubeclient.core.network.sponsorblock.SegmentDto
import dev.opux.tubeclient.core.network.sponsorblock.SponsorBlockApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SponsorBlockRepositoryImplTest {

    @Test
    fun `returns empty list without hitting the network when all categories disabled`() = runTest {
        val api = mockk<SponsorBlockApi>(relaxed = true)
        val prefs = mockk<SponsorBlockPreferences> {
            every { enabledCategories } returns flowOf(emptySet())
        }

        val repo = SponsorBlockRepositoryImpl(api, prefs)
        val result = repo.getSkipSegments("abc")

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Any>(), result.getOrThrow())
        coVerify(exactly = 0) { api.getSkipSegments(any(), any()) }
    }

    @Test
    fun `passes only enabled categories to the API`() = runTest {
        val api = mockk<SponsorBlockApi>()
        val captured = slot<String>()
        coEvery { api.getSkipSegments(any(), capture(captured)) } returns emptyList()
        val prefs = mockk<SponsorBlockPreferences> {
            every { enabledCategories } returns flowOf(
                setOf(SponsorBlockCategory.SPONSOR, SponsorBlockCategory.OUTRO),
            )
        }

        val repo = SponsorBlockRepositoryImpl(api, prefs)
        val result = repo.getSkipSegments("abc")

        assertTrue(result.isSuccess)
        val query = captured.captured
        assertTrue("query is a JSON-ish list: $query", query.startsWith("[") && query.endsWith("]"))
        assertTrue("includes sponsor", query.contains("\"sponsor\""))
        assertTrue("includes outro", query.contains("\"outro\""))
        assertTrue("does not include intro", !query.contains("\"intro\""))
        assertTrue("does not include selfpromo", !query.contains("\"selfpromo\""))
    }

    @Test
    fun `api success surfaces mapped segments in seconds-to-millis form`() = runTest {
        val api = mockk<SponsorBlockApi>()
        coEvery { api.getSkipSegments(any(), any()) } returns listOf(
            SegmentDto(
                category = "sponsor",
                actionType = "skip",
                segment = listOf(12.5, 24.0),
                uuid = "u1",
            ),
        )
        val prefs = mockk<SponsorBlockPreferences> {
            every { enabledCategories } returns flowOf(setOf(SponsorBlockCategory.SPONSOR))
        }

        val repo = SponsorBlockRepositoryImpl(api, prefs)
        val segments = repo.getSkipSegments("abc").getOrThrow()

        assertEquals(1, segments.size)
        assertEquals(12_500L, segments.first().startMs)
        assertEquals(24_000L, segments.first().endMs)
        assertEquals("sponsor", segments.first().category)
    }

    @Test
    fun `api failure becomes a Failure result so callers can fall back gracefully`() = runTest {
        val api = mockk<SponsorBlockApi>()
        coEvery { api.getSkipSegments(any(), any()) } throws RuntimeException("404")
        val prefs = mockk<SponsorBlockPreferences> {
            every { enabledCategories } returns flowOf(setOf(SponsorBlockCategory.SPONSOR))
        }

        val repo = SponsorBlockRepositoryImpl(api, prefs)
        val result = repo.getSkipSegments("abc")

        assertTrue(result.isFailure)
    }
}
