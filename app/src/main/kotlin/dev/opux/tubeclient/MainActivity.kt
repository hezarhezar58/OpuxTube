package dev.opux.tubeclient

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import dagger.hilt.android.AndroidEntryPoint
import dev.opux.tubeclient.core.player.MediaPlayerController
import dev.opux.tubeclient.core.ui.theme.OpuxTubeTheme
import dev.opux.tubeclient.core.ui.util.LocalIsInPipMode
import dev.opux.tubeclient.navigation.OpuxNavGraph
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var controller: MediaPlayerController

    private var isInPipMode by mutableStateOf(false)

    private var attachedPlayer: Player? = null

    private val pipPlayerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isInPipMode) updatePipParams()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (isInPipMode) updatePipParams()
        }
    }

    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_PIP_CONTROL) return
            val player = controller.playerFlow.value ?: return
            when (intent.getIntExtra(EXTRA_CONTROL_TYPE, -1)) {
                CONTROL_TOGGLE_PLAY ->
                    if (player.isPlaying) controller.pause() else controller.resume()

                CONTROL_SEEK_BACK -> {
                    val target = (player.currentPosition - SEEK_DELTA_MS).coerceAtLeast(0L)
                    controller.seekTo(target)
                }

                CONTROL_SEEK_FORWARD -> {
                    val raw = player.currentPosition + SEEK_DELTA_MS
                    val duration = player.duration
                    val target = if (duration > 0L) raw.coerceAtMost(duration) else raw
                    controller.seekTo(target)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpuxTubeTheme {
                CompositionLocalProvider(LocalIsInPipMode provides isInPipMode) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        OpuxNavGraph()
                    }
                }
            }
        }
        registerPipReceiver()
        observePlayerForPipUpdates()
    }

    private fun registerPipReceiver() {
        val filter = IntentFilter(ACTION_PIP_CONTROL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pipReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(pipReceiver, filter)
        }
    }

    private fun observePlayerForPipUpdates() {
        lifecycleScope.launch {
            controller.playerFlow.collect { newPlayer ->
                attachedPlayer?.removeListener(pipPlayerListener)
                attachedPlayer = newPlayer
                newPlayer?.addListener(pipPlayerListener)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        maybeEnterPip()
    }

    private fun maybeEnterPip() {
        if (isInPipMode) return
        val player = controller.playerFlow.value ?: return
        if (!player.isPlaying) return
        val width = player.videoSize.width
        val height = player.videoSize.height
        if (width <= 0 || height <= 0) return
        val params = buildPipParams(player, Rational(width, height).coerceInPipBounds())
        runCatching { enterPictureInPictureMode(params) }
    }

    private fun updatePipParams() {
        val player = controller.playerFlow.value ?: return
        val width = player.videoSize.width.takeIf { it > 0 } ?: return
        val height = player.videoSize.height.takeIf { it > 0 } ?: return
        val params = buildPipParams(player, Rational(width, height).coerceInPipBounds())
        runCatching { setPictureInPictureParams(params) }
    }

    private fun buildPipParams(player: Player, aspect: Rational): PictureInPictureParams =
        PictureInPictureParams.Builder()
            .setAspectRatio(aspect)
            .setActions(buildPipActions(player.isPlaying))
            .build()

    private fun buildPipActions(isPlaying: Boolean): List<RemoteAction> = listOf(
        remoteAction(
            controlType = CONTROL_SEEK_BACK,
            iconRes = android.R.drawable.ic_media_rew,
            title = "Geri 10s",
            description = "10 saniye geri sar",
            requestCode = REQUEST_SEEK_BACK,
        ),
        remoteAction(
            controlType = CONTROL_TOGGLE_PLAY,
            iconRes = if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play,
            title = if (isPlaying) "Duraklat" else "Oynat",
            description = if (isPlaying) "Duraklat" else "Oynat",
            requestCode = REQUEST_TOGGLE,
        ),
        remoteAction(
            controlType = CONTROL_SEEK_FORWARD,
            iconRes = android.R.drawable.ic_media_ff,
            title = "İleri 10s",
            description = "10 saniye ileri sar",
            requestCode = REQUEST_SEEK_FORWARD,
        ),
    )

    private fun remoteAction(
        controlType: Int,
        iconRes: Int,
        title: String,
        description: String,
        requestCode: Int,
    ): RemoteAction {
        val intent = Intent(ACTION_PIP_CONTROL)
            .setPackage(packageName)
            .putExtra(EXTRA_CONTROL_TYPE, controlType)
        val pending = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return RemoteAction(
            Icon.createWithResource(this, iconRes),
            title,
            description,
            pending,
        )
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
    }

    override fun onDestroy() {
        attachedPlayer?.removeListener(pipPlayerListener)
        attachedPlayer = null
        runCatching { unregisterReceiver(pipReceiver) }
        super.onDestroy()
    }

    companion object {
        private const val ACTION_PIP_CONTROL = "dev.opux.tubeclient.PIP_CONTROL"
        private const val EXTRA_CONTROL_TYPE = "control_type"
        private const val CONTROL_TOGGLE_PLAY = 1
        private const val CONTROL_SEEK_BACK = 2
        private const val CONTROL_SEEK_FORWARD = 3
        private const val REQUEST_SEEK_BACK = 101
        private const val REQUEST_TOGGLE = 102
        private const val REQUEST_SEEK_FORWARD = 103
        private const val SEEK_DELTA_MS = 10_000L
    }
}

private fun Rational.coerceInPipBounds(): Rational {
    val ratio = numerator.toDouble() / denominator.toDouble()
    return when {
        ratio > 2.39 -> Rational(239, 100)
        ratio < 1.0 / 2.39 -> Rational(100, 239)
        else -> this
    }
}
