package dev.opux.tubeclient.core.ui.util

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * True while the activity is in Picture-in-Picture mode. Screens use this to switch to a
 * minimal video-only layout — no top bar, no descriptions, no list — so the PiP window
 * doesn't try to render unrelated UI in a tiny rectangle.
 */
val LocalIsInPipMode = staticCompositionLocalOf { false }
