package com.vishalgaur.musicplayer.exomusicplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.vishalgaur.musicplayer.network.SongDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


enum class FirebaseStatus { CREATED, INITIALIZING, INITIALIZED, ERROR }

class MusicSource(private val songDatabase: SongDatabase) {

    var songs = emptyList<MediaMetadataCompat>()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var status: FirebaseStatus = FirebaseStatus.CREATED
        set(value) {
            if (value == FirebaseStatus.INITIALIZED || value == FirebaseStatus.ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach {
                        it(status == FirebaseStatus.INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        status = FirebaseStatus.INITIALIZING
        val allSongs = songDatabase.getSongs()
        songs = allSongs.map { song ->
            Builder()
                .putString(METADATA_KEY_ARTIST, song.artist.joinToString { name -> name })
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_MEDIA_ID, song.songId.toString())
                .putString(METADATA_KEY_MEDIA_URI, song.url)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.banner)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.banner)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.artist.joinToString { name -> name })
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.title)
                .build()
        }

        status = FirebaseStatus.INITIALIZED
    }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (status == FirebaseStatus.CREATED || status == FirebaseStatus.INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(status == FirebaseStatus.INITIALIZED)
            true
        }
    }

    // making a playlist of songs
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaItem = MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())  // deprecated use createMediaSource(mediaItem) instead
                .createMediaSource(mediaItem)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val description = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)
    }.toMutableList()
}