package dev.opux.tubeclient.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.opux.tubeclient.core.database.dao.DownloadedVideoDao
import dev.opux.tubeclient.core.database.dao.PlaylistDao
import dev.opux.tubeclient.core.database.dao.SubscriptionDao
import dev.opux.tubeclient.core.database.dao.WatchHistoryDao
import dev.opux.tubeclient.core.database.entity.DownloadedVideoEntity
import dev.opux.tubeclient.core.database.entity.PlaylistEntity
import dev.opux.tubeclient.core.database.entity.PlaylistEntryEntity
import dev.opux.tubeclient.core.database.entity.SubscriptionEntity
import dev.opux.tubeclient.core.database.entity.WatchHistoryEntity

@Database(
    entities = [
        SubscriptionEntity::class,
        WatchHistoryEntity::class,
        PlaylistEntity::class,
        PlaylistEntryEntity::class,
        DownloadedVideoEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadedVideoDao(): DownloadedVideoDao

    companion object {
        const val DB_NAME = "opux_tube.db"
    }
}
