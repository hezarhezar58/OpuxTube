package dev.opux.tubeclient.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.opux.tubeclient.core.domain.repository.ChannelRepository
import dev.opux.tubeclient.core.domain.repository.SearchRepository
import dev.opux.tubeclient.core.domain.repository.SponsorBlockRepository
import dev.opux.tubeclient.core.domain.repository.SubscriptionRepository
import dev.opux.tubeclient.core.domain.repository.TrendingRepository
import dev.opux.tubeclient.core.domain.repository.VideoRepository
import dev.opux.tubeclient.core.domain.repository.WatchHistoryRepository
import dev.opux.tubeclient.core.domain.usecase.ClearWatchHistoryUseCase
import dev.opux.tubeclient.core.domain.usecase.GetChannelDetailsUseCase
import dev.opux.tubeclient.core.domain.usecase.GetChannelVideosUseCase
import dev.opux.tubeclient.core.domain.usecase.GetLastPositionUseCase
import dev.opux.tubeclient.core.domain.usecase.GetSkipSegmentsUseCase
import dev.opux.tubeclient.core.domain.usecase.GetSubscriptionsUseCase
import dev.opux.tubeclient.core.domain.usecase.GetTrendingUseCase
import dev.opux.tubeclient.core.domain.usecase.GetVideoDetailsUseCase
import dev.opux.tubeclient.core.domain.usecase.GetWatchHistoryUseCase
import dev.opux.tubeclient.core.domain.usecase.IsSubscribedUseCase
import dev.opux.tubeclient.core.domain.usecase.RecordWatchEventUseCase
import dev.opux.tubeclient.core.domain.usecase.SearchVideosUseCase
import dev.opux.tubeclient.core.domain.usecase.SubscribeChannelUseCase
import dev.opux.tubeclient.core.domain.usecase.UnsubscribeChannelUseCase
import dev.opux.tubeclient.core.domain.usecase.UpdateWatchProgressUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideSearchVideosUseCase(repo: SearchRepository) = SearchVideosUseCase(repo)

    @Provides
    @Singleton
    fun provideGetVideoDetailsUseCase(repo: VideoRepository) = GetVideoDetailsUseCase(repo)

    @Provides
    @Singleton
    fun provideGetTrendingUseCase(repo: TrendingRepository) = GetTrendingUseCase(repo)

    @Provides
    @Singleton
    fun provideRecordWatchEventUseCase(repo: WatchHistoryRepository) = RecordWatchEventUseCase(repo)

    @Provides
    @Singleton
    fun provideGetWatchHistoryUseCase(repo: WatchHistoryRepository) = GetWatchHistoryUseCase(repo)

    @Provides
    @Singleton
    fun provideClearWatchHistoryUseCase(repo: WatchHistoryRepository) = ClearWatchHistoryUseCase(repo)

    @Provides
    @Singleton
    fun provideUpdateWatchProgressUseCase(repo: WatchHistoryRepository) = UpdateWatchProgressUseCase(repo)

    @Provides
    @Singleton
    fun provideGetLastPositionUseCase(repo: WatchHistoryRepository) = GetLastPositionUseCase(repo)

    @Provides
    @Singleton
    fun provideGetChannelDetailsUseCase(repo: ChannelRepository) = GetChannelDetailsUseCase(repo)

    @Provides
    @Singleton
    fun provideGetChannelVideosUseCase(repo: ChannelRepository) = GetChannelVideosUseCase(repo)

    @Provides
    @Singleton
    fun provideGetSubscriptionsUseCase(repo: SubscriptionRepository) = GetSubscriptionsUseCase(repo)

    @Provides
    @Singleton
    fun provideIsSubscribedUseCase(repo: SubscriptionRepository) = IsSubscribedUseCase(repo)

    @Provides
    @Singleton
    fun provideSubscribeChannelUseCase(repo: SubscriptionRepository) = SubscribeChannelUseCase(repo)

    @Provides
    @Singleton
    fun provideUnsubscribeChannelUseCase(repo: SubscriptionRepository) = UnsubscribeChannelUseCase(repo)

    @Provides
    @Singleton
    fun provideGetSkipSegmentsUseCase(repo: SponsorBlockRepository) = GetSkipSegmentsUseCase(repo)
}
