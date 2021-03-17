package com.vishalgaur.musicplayer.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.vishalgaur.musicplayer.R
import com.vishalgaur.musicplayer.exomusicplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

	@Singleton
	@Provides
	fun provideMusicServiceConnection(@ApplicationContext context: Context) =
			MusicServiceConnection(context)

	@Singleton
	@Provides
	fun provideGlideInstance(
            @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
            RequestOptions()
                    .placeholder(R.drawable.ic_baseline_music_note_24)
                    .error(R.drawable.ic_baseline_music_note_24)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
    )
}