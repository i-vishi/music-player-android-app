package com.vishalgaur.musicplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vishalgaur.musicplayer.exomusicplayer.MusicServiceConnection
import com.vishalgaur.musicplayer.network.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject



@HiltViewModel
class MainViewModel @Inject constructor (private val musicServiceConnection: MusicServiceConnection) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> get() = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currPlayingSong = musicServiceConnection.currPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.value = Resource.loading(null)
        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map { mediaItem ->
                        Song(
                            mediaItem.mediaId!!,
                            mediaItem.description.mediaUri.toString(),
                            mediaItem.description.title.toString(),
                            mediaItem.description.iconUri.toString(),
                            ArrayList(mediaItem.description.subtitle?.split(",")?.map { it.trim() })
                        )
                    }
                    _mediaItems.value = Resource.success(items)
                }
            })
    }

    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekSongTo(position: Long) {
        musicServiceConnection.transportControls.seekTo(position)
    }

    fun playOrPauseSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.songId== currPlayingSong.value?.getString(
                METADATA_KEY_MEDIA_ID
            )
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(
                mediaItem.songId,
                null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}