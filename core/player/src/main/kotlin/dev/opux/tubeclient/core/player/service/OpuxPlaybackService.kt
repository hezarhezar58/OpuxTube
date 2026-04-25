package dev.opux.tubeclient.core.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class OpuxPlaybackService : MediaSessionService() {

    private lateinit var httpFactory: DataSource.Factory
    private lateinit var progressiveFactory: ProgressiveMediaSource.Factory
    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(USER_AGENT)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)
            .setAllowCrossProtocolRedirects(true)
        progressiveFactory = ProgressiveMediaSource.Factory(httpFactory)

        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Timber.e(error, "PlaybackService player error")
                    }
                })
            }

        ensureNotificationChannel()
        startForeground(
            PLACEHOLDER_NOTIFICATION_ID,
            buildPlaceholderNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this).build(),
        )

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val sessionActivity = launchIntent?.let { intent ->
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SessionCallback())
            .apply { sessionActivity?.let { setSessionActivity(it) } }
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        // Free the service when the app is swiped away and nothing is actively playing.
        val p = mediaSession?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private inner class SessionCallback : MediaSession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                .buildUpon()
                .add(SessionCommand(CMD_PLAY_STREAMS, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> = when (customCommand.customAction) {
            CMD_PLAY_STREAMS -> handlePlayStreams(args)
            else -> super.onCustomCommand(session, controller, customCommand, args)
        }

        private fun handlePlayStreams(args: Bundle): ListenableFuture<SessionResult> {
            val videoUrl = args.getString(ARG_VIDEO_URL)
            if (videoUrl.isNullOrBlank()) {
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE))
            }
            val audioUrl = args.getString(ARG_AUDIO_URL)
            val startMs = args.getLong(ARG_START_MS, 0L)
            val title = args.getString(ARG_TITLE)
            val artist = args.getString(ARG_ARTIST)
            val artworkUrl = args.getString(ARG_ARTWORK_URL)

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .apply { artworkUrl?.let { setArtworkUri(Uri.parse(it)) } }
                .build()
            val videoMediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaMetadata(mediaMetadata)
                .build()

            val source: MediaSource = if (!audioUrl.isNullOrBlank()) {
                val v = progressiveFactory.createMediaSource(videoMediaItem)
                val a = progressiveFactory.createMediaSource(MediaItem.fromUri(audioUrl))
                MergingMediaSource(v, a)
            } else {
                progressiveFactory.createMediaSource(videoMediaItem)
            }

            player.setMediaSource(source)
            player.prepare()
            if (startMs > 0L) {
                player.seekTo(startMs)
            }
            player.playWhenReady = true

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun ensureNotificationChannel() {
        val mgr = getSystemService(NotificationManager::class.java) ?: return
        if (mgr.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID, "Oynatma", NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "OpuxTube oynatma denetimleri"
            setShowBadge(false)
        }
        mgr.createNotificationChannel(channel)
    }

    private fun buildPlaceholderNotification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("OpuxTube")
            .setContentText("Hazırlanıyor…")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    companion object {
        const val CMD_PLAY_STREAMS = "dev.opux.PLAY_STREAMS"
        const val ARG_VIDEO_URL = "videoUrl"
        const val ARG_AUDIO_URL = "audioUrl"
        const val ARG_START_MS = "startMs"
        const val ARG_TITLE = "title"
        const val ARG_ARTIST = "artist"
        const val ARG_ARTWORK_URL = "artworkUrl"

        private const val CHANNEL_ID = "opux_playback"
        private const val PLACEHOLDER_NOTIFICATION_ID = 0xC100
        private const val USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
}
