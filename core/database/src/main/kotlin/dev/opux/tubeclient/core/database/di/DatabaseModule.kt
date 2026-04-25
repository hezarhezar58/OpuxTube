package dev.opux.tubeclient.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.opux.tubeclient.core.database.AppDatabase
import dev.opux.tubeclient.core.database.dao.DownloadedVideoDao
import dev.opux.tubeclient.core.database.dao.PlaylistDao
import dev.opux.tubeclient.core.database.dao.SubscriptionDao
import dev.opux.tubeclient.core.database.dao.WatchHistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSubscriptionDao(db: AppDatabase): SubscriptionDao = db.subscriptionDao()

    @Provides
    fun provideWatchHistoryDao(db: AppDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideDownloadedVideoDao(db: AppDatabase): DownloadedVideoDao = db.downloadedVideoDao()
}
