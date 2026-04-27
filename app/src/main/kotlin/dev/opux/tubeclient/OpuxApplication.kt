package dev.opux.tubeclient

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class OpuxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Release: Sentry's manifest auto-init wires up its own Timber integration
        // (via the io.sentry:sentry-android-timber dependency). Uncaught exceptions
        // become Sentry events; Timber.w/e calls become events too.
    }
}
