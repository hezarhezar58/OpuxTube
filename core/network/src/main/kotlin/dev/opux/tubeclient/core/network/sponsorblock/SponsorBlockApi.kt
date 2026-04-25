package dev.opux.tubeclient.core.network.sponsorblock

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface SponsorBlockApi {

    @GET("api/skipSegments")
    suspend fun getSkipSegments(
        @Query("videoID") videoId: String,
        @Query("categories") categories: String = DEFAULT_CATEGORIES,
    ): List<SegmentDto>

    companion object {
        const val BASE_URL = "https://sponsor.ajay.app/"
        const val DEFAULT_CATEGORIES =
            "[\"sponsor\",\"intro\",\"outro\",\"selfpromo\",\"interaction\",\"music_offtopic\"]"
    }
}

@Serializable
data class SegmentDto(
    @SerialName("category") val category: String,
    @SerialName("actionType") val actionType: String? = null,
    @SerialName("segment") val segment: List<Double>,
    @SerialName("UUID") val uuid: String,
    @SerialName("videoDuration") val videoDuration: Double = 0.0,
    @SerialName("locked") val locked: Int = 0,
    @SerialName("votes") val votes: Int = 0,
    @SerialName("description") val description: String = "",
) {
    val startSeconds: Double get() = segment.getOrNull(0) ?: 0.0
    val endSeconds: Double get() = segment.getOrNull(1) ?: 0.0
}
