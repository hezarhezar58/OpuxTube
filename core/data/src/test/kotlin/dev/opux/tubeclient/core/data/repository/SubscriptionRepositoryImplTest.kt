package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.database.dao.SubscriptionDao
import dev.opux.tubeclient.core.database.entity.SubscriptionEntity
import dev.opux.tubeclient.core.domain.model.ChannelDetail
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionRepositoryImplTest {

    @Test
    fun `observeAll maps entities to domain models in DAO order`() = runTest {
        val dao = mockk<SubscriptionDao>()
        every { dao.observeAll() } returns flowOf(
            listOf(
                entity(channelUrl = "https://yt/ch/a"),
                entity(channelUrl = "https://yt/ch/b"),
            ),
        )

        val repo = SubscriptionRepositoryImpl(dao)
        val items = repo.observeAll().first()

        assertEquals(listOf("https://yt/ch/a", "https://yt/ch/b"), items.map { it.channelUrl })
    }

    @Test
    fun `observeIsSubscribed dedupes consecutive duplicate states`() = runTest {
        val dao = mockk<SubscriptionDao>()
        val target = "https://yt/ch/x"
        // Five DAO emissions but only three distinct membership states; distinctUntilChanged
        // should compress them to [false, true, false].
        every { dao.observeAll() } returns flowOf(
            emptyList(),
            listOf(entity(channelUrl = target)),
            listOf(entity(channelUrl = target)),
            listOf(entity(channelUrl = target), entity(channelUrl = "https://yt/ch/other")),
            emptyList(),
        )

        val repo = SubscriptionRepositoryImpl(dao)
        val emissions = repo.observeIsSubscribed(target).toList()

        assertEquals(listOf(false, true, false), emissions)
    }

    @Test
    fun `subscribe upserts a SubscriptionEntity built from the channel detail`() = runTest {
        val dao = mockk<SubscriptionDao>(relaxed = true)
        val repo = SubscriptionRepositoryImpl(dao)
        val captured = slot<SubscriptionEntity>()
        coEvery { dao.upsert(capture(captured)) } returns Unit

        repo.subscribe(
            ChannelDetail(
                url = "https://yt/ch/z",
                name = "Zed",
                avatarUrl = "https://img/avatar",
                bannerUrl = null,
                description = "desc",
                subscriberCount = 42L,
                isVerified = false,
            ),
        )

        coVerify { dao.upsert(any()) }
        val saved = captured.captured
        assertEquals("https://yt/ch/z", saved.channelUrl)
        assertEquals("Zed", saved.name)
        assertEquals("https://img/avatar", saved.avatarUrl)
        assertEquals(42L, saved.subscriberCount)
        assertTrue("subscribedAt should be a real wall-clock timestamp", saved.subscribedAt > 0L)
    }

    @Test
    fun `unsubscribe forwards the channel URL to the DAO`() = runTest {
        val dao = mockk<SubscriptionDao>(relaxed = true)
        val repo = SubscriptionRepositoryImpl(dao)

        repo.unsubscribe("https://yt/ch/gone")

        coVerify { dao.delete("https://yt/ch/gone") }
    }

    @Test
    fun `observeIsSubscribed initially false when DAO emits empty list`() = runTest {
        val dao = mockk<SubscriptionDao>()
        every { dao.observeAll() } returns flowOf(emptyList())

        val repo = SubscriptionRepositoryImpl(dao)
        val initial = repo.observeIsSubscribed("https://yt/ch/x").first()

        assertFalse(initial)
    }

    private fun entity(
        channelUrl: String,
        name: String = "Channel",
        avatarUrl: String? = null,
        subscriberCount: Long = 0L,
        subscribedAt: Long = 1_000L,
    ): SubscriptionEntity = SubscriptionEntity(
        channelUrl = channelUrl,
        name = name,
        avatarUrl = avatarUrl,
        subscriberCount = subscriberCount,
        subscribedAt = subscribedAt,
    )
}
