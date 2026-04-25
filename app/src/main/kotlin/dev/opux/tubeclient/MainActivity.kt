package dev.opux.tubeclient

import android.app.PictureInPictureParams
import android.content.res.Configuration
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
import dagger.hilt.android.AndroidEntryPoint
import dev.opux.tubeclient.core.player.MediaPlayerController
import dev.opux.tubeclient.core.ui.theme.OpuxTubeTheme
import dev.opux.tubeclient.core.ui.util.LocalIsInPipMode
import dev.opux.tubeclient.navigation.OpuxNavGraph
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var controller: MediaPlayerController

    private var isInPipMode by mutableStateOf(false)

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
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Enter PiP only when the user clearly wants to leave AND there's an active video
        // worth keeping on screen — audio-only content doesn't benefit from PiP.
        maybeEnterPip()
    }

    private fun maybeEnterPip() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (isInPipMode) return
        val player = controller.playerFlow.value ?: return
        if (!player.isPlaying) return
        val width = player.videoSize.width
        val height = player.videoSize.height
        if (width <= 0 || height <= 0) return
        // PiP aspect ratio is clamped by the platform to roughly between 1:2.39 and 2.39:1.
        val aspect = Rational(width, height).coerceInPipBounds()
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(aspect)
            .build()
        runCatching { enterPictureInPictureMode(params) }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
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
