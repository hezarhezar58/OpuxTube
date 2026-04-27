package dev.opux.tubeclient.core.player

import dev.opux.tubeclient.core.domain.model.AudioStream
import dev.opux.tubeclient.core.domain.model.ChannelSummary
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamSelectorTest {

    private val selector = StreamSelector()

    @Test
    fun `prefers progressive stream within preferred range`() {
        val detail = videoDetail(
            videoStreams = listOf(
                videoStream(url = "p360", height = 360),
                videoStream(url = "p720", height = 720),
            ),
        )

        val selection = selector.select(detail)

        assertTrue(selection is StreamSelector.Selection.Progressive)
        assertEquals("p720", (selection as StreamSelector.Selection.Progressive).video.url)
    }

    @Test
    fun `falls back to merged video-only when no progressive`() {
        val detail = videoDetail(
            videoStreams = emptyList(),
            videoOnlyStreams = listOf(videoStream(url = "vo720", height = 720)),
            audioStreams = listOf(audio(url = "a160", bitrate = 160)),
        )

        val selection = selector.select(detail)

        assertTrue(selection is StreamSelector.Selection.Merged)
        val merged = selection as StreamSelector.Selection.Merged
        assertEquals("vo720", merged.video.url)
        assertEquals("a160", merged.audio.url)
    }

    @Test
    fun `override matching progressive list is treated as Progressive`() {
        val override = videoStream(url = "p1080", height = 1080)
        val detail = videoDetail(
            videoStreams = listOf(
                videoStream(url = "p720", height = 720),
                override,
            ),
        )

        val selection = selector.select(detail, override)

        assertTrue(selection is StreamSelector.Selection.Progressive)
        assertEquals("p1080", (selection as StreamSelector.Selection.Progressive).video.url)
    }

    @Test
    fun `override that points at video-only track gets paired with best audio`() {
        val override = videoStream(url = "vo2160", height = 2160)
        val detail = videoDetail(
            videoStreams = listOf(videoStream(url = "p720", height = 720)),
            videoOnlyStreams = listOf(override),
            audioStreams = listOf(
                audio(url = "a128", bitrate = 128),
                audio(url = "a160", bitrate = 160),
            ),
        )

        val selection = selector.select(detail, override)

        assertTrue(selection is StreamSelector.Selection.Merged)
        val merged = selection as StreamSelector.Selection.Merged
        assertEquals("vo2160", merged.video.url)
        // Highest bitrate audio wins.
        assertEquals("a160", merged.audio.url)
    }

    @Test
    fun `local file override is treated as progressive without merging audio`() {
        val override = videoStream(url = "file:///data/user/0/dev.opux/files/downloads/abc.mp4", height = 0)
        val detail = videoDetail(
            videoStreams = listOf(videoStream(url = "p720", height = 720)),
            audioStreams = listOf(audio(url = "a128", bitrate = 128)),
        )

        val selection = selector.select(detail, override)

        assertTrue(selection is StreamSelector.Selection.Progressive)
        assertEquals(override.url, (selection as StreamSelector.Selection.Progressive).video.url)
    }

    @Test
    fun `select returns null when no streams at all`() {
        val detail = videoDetail()
        assertNull(selector.select(detail))
    }

    private fun videoDetail(
        videoStreams: List<VideoStream> = emptyList(),
        videoOnlyStreams: List<VideoStream> = emptyList(),
        audioStreams: List<AudioStream> = emptyList(),
    ): VideoDetail = VideoDetail(
        id = "id",
        url = "https://example.com/watch?v=id",
        title = "t",
        description = "",
        thumbnailUrl = null,
        durationSeconds = 100,
        viewCount = 0,
        likeCount = 0,
        uploadedAt = null,
        category = null,
        channel = ChannelSummary(
            name = "ch",
            url = "https://example.com/ch",
            avatarUrl = null,
            subscriberCount = 0,
            isVerified = false,
        ),
        videoStreams = videoStreams,
        audioStreams = audioStreams,
        videoOnlyStreams = videoOnlyStreams,
        relatedVideos = emptyList(),
        isLive = false,
    )

    private fun videoStream(url: String, height: Int): VideoStream = VideoStream(
        url = url,
        mimeType = "video/mp4",
        resolution = "${height}p",
        width = (height * 16) / 9,
        height = height,
        bitrate = height * 1000,
        framerate = 30,
        codec = "avc1",
        isAdaptive = false,
    )

    private fun audio(url: String, bitrate: Int): AudioStream = AudioStream(
        url = url,
        mimeType = "audio/mp4",
        bitrate = bitrate,
        averageBitrate = bitrate,
        codec = "mp4a",
        language = null,
    )
}
