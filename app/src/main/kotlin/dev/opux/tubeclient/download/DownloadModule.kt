package dev.opux.tubeclient.download

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.opux.tubeclient.core.domain.repository.DownloadActions
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadModule {

    @Binds
    @Singleton
    abstract fun bindDownloadActions(impl: DownloadCoordinator): DownloadActions
}
