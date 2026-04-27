package dev.opux.tubeclient.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.schabi.newpipe.extractor.Image

class ExtractorMappersTest {

    @Test
    fun `bestThumbnailUrl returns null for null input`() {
        val list: List<Image>? = null
        assertNull(list.bestThumbnailUrl())
    }

    @Test
    fun `bestThumbnailUrl returns null for empty list`() {
        assertNull(emptyList<Image>().bestThumbnailUrl())
    }

    @Test
    fun `bestThumbnailUrl picks the largest image by area`() {
        val small = Image("https://example.com/s.jpg", 90, 120, Image.ResolutionLevel.LOW)
        val medium = Image("https://example.com/m.jpg", 180, 320, Image.ResolutionLevel.MEDIUM)
        val large = Image("https://example.com/l.jpg", 720, 1280, Image.ResolutionLevel.HIGH)

        // Note: Image constructor takes (url, height, width, level).
        val picked = listOf(small, medium, large).bestThumbnailUrl()

        assertEquals("https://example.com/l.jpg", picked)
    }

    @Test
    fun `bestThumbnailUrl falls back when widths or heights are unknown`() {
        val unknownDims = Image(
            "https://example.com/u.jpg",
            Image.HEIGHT_UNKNOWN,
            Image.WIDTH_UNKNOWN,
            Image.ResolutionLevel.UNKNOWN,
        )
        val tiny = Image("https://example.com/t.jpg", 24, 32, Image.ResolutionLevel.LOW)

        // Unknown dimensions resolve to a 0×0 area, so the tiny image with real
        // dimensions wins over the unknown-dimensioned one.
        val picked = listOf(unknownDims, tiny).bestThumbnailUrl()

        assertEquals("https://example.com/t.jpg", picked)
    }
}
