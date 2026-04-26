package dev.opux.tubeclient.core.data.mapper

import dev.opux.tubeclient.core.domain.model.SkipSegment
import dev.opux.tubeclient.core.network.sponsorblock.SegmentDto

fun SegmentDto.toDomain(): SkipSegment = SkipSegment(
    uuid = uuid,
    category = category,
    startMs = (startSeconds * 1000.0).toLong(),
    endMs = (endSeconds * 1000.0).toLong(),
)
