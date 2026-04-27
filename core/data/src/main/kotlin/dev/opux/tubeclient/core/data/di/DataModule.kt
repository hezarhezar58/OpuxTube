package dev.opux.tubeclient.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.opux.tubeclient.core.data.preferences.SponsorBlockPreferencesImpl
import dev.opux.tubeclient.core.data.repository.ChannelRepositoryImpl
import dev.opux.tubeclient.core.data.repository.DownloadRepositoryImpl
import dev.opux.tubeclient.core.data.repository.PlaylistRepositoryImpl
import dev.opux.tubeclient.core.data.repository.SearchRepositoryImpl
import dev.opux.tubeclient.core.data.repository.SponsorBlockRepositoryImpl
import dev.opux.tubeclient.core.data.repository.SubscriptionRepositoryImpl
import dev.opux.tubeclient.core.data.repository.TrendingRepositoryImpl
import dev.opux.tubeclient.core.data.repository.VideoRepositoryImpl
import dev.opux.tubeclient.core.data.repository.WatchHistoryRepositoryImpl
import dev.opux.tubeclient.core.domain.preferences.SponsorBlockPreferences
import dev.opux.tubeclient.core.domain.repository.ChannelRepository
import dev.opux.tubeclient.core.domain.repository.DownloadRepository
import dev.opux.tubeclient.core.domain.repository.PlaylistRepository
import dev.opux.tubeclient.core.domain.repository.SearchRepository
import dev.opux.tubeclient.core.domain.repository.SponsorBlockRepository
import dev.opux.tubeclient.core.domain.repository.SubscriptionRepository
import dev.opux.tubeclient.core.domain.repository.TrendingRepository
import dev.opux.tubeclient.core.domain.repository.VideoRepository
import dev.opux.tubeclient.core.domain.repository.WatchHistoryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindTrendingRepository(impl: TrendingRepositoryImpl): TrendingRepository

    @Binds
    @Singleton
    abstract fun bindWatchHistoryRepository(impl: WatchHistoryRepositoryImpl): WatchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindChannelRepository(impl: ChannelRepositoryImpl): ChannelRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindSponsorBlockRepository(impl: SponsorBlockRepositoryImpl): SponsorBlockRepository

    @Binds
    @Singleton
    abstract fun bindSponsorBlockPreferences(impl: SponsorBlockPreferencesImpl): SponsorBlockPreferences

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository
}
