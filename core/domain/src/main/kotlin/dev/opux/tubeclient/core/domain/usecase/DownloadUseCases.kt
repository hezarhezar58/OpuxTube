package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.DownloadedVideo
import dev.opux.tubeclient.core.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow

class ObserveDownloadsUseCase(private val repo: DownloadRepository) {
    operator fun invoke(): Flow<List<DownloadedVideo>> = repo.observeAll()
}

class FindDownloadUseCase(private val repo: DownloadRepository) {
    suspend operator fun invoke(videoId: String): DownloadedVideo? = repo.find(videoId)
}

class DeleteDownloadUseCase(private val repo: DownloadRepository) {
    suspend operator fun invoke(videoId: String) = repo.delete(videoId)
}
