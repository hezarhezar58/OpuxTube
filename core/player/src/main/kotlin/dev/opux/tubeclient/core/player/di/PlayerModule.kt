package dev.opux.tubeclient.core.player.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.opux.tubeclient.core.player.ExoMediaPlayerController
import dev.opux.tubeclient.core.player.MediaPlayerController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindMediaPlayerController(impl: ExoMediaPlayerController): MediaPlayerController
}
