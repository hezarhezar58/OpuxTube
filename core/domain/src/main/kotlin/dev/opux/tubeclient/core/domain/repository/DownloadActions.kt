package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.DownloadStatus
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoStream
import kotlinx.coroutines.flow.StateFlow

interface DownloadActions {
    val statuses: StateFlow<Map<String, DownloadStatus>>
    fun enqueue(detail: VideoDetail, stream: VideoStream): Boolean
}
