package com.vishalgaur.musicplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vishalgaur.musicplayer.exomusicplayer.MusicServiceConnection
import com.vishalgaur.musicplayer.network.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "MAINVIEWMODEL"


@HiltViewModel
class MainViewModel @Inject constructor(private val musicServiceConnection: MusicServiceConnection) :
		ViewModel() {

	private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
	val mediaItems: LiveData<Resource<List<Song>>> get() = _mediaItems

	val isConnected = musicServiceConnection.isConnected
	val networkError = musicServiceConnection.networkError
	val currPlayingSong = musicServiceConnection.currPlayingSong
	val playbackState = musicServiceConnection.playbackState
	val shuffleState = musicServiceConnection.shuffleState
	val repeatState = musicServiceConnection.repeatState
	val playbackSpeed = musicServiceConnection.playbackSpeed

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
		if (isPrepared && mediaItem.songId == currPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
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

	fun toggleShuffleState() {
		Log.d(TAG, "shuffle: ${shuffleState.value}")
		if (shuffleState.value != null) {
			shuffleState.value?.let {
				when (it) {
					0 -> musicServiceConnection.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
					1 -> musicServiceConnection.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
					-1 -> musicServiceConnection.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
					else -> musicServiceConnection.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
				}
			}
		} else {
			musicServiceConnection.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
		}
	}

	fun toggleRepeatState() {
		Log.d(TAG, "repeat: ${repeatState.value}")
		if (repeatState.value != null) {
			repeatState.value?.let {
				when (it) {
					-1 -> musicServiceConnection.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
					0 -> musicServiceConnection.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
					2 -> musicServiceConnection.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
					1 -> musicServiceConnection.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
					else -> musicServiceConnection.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
				}
			}
		} else {
			musicServiceConnection.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
		}
	}

	fun togglePlaybackSpeed() {
		if (playbackSpeed.value != null) {
			playbackSpeed.value?.let {
				when (it) {
					1.0f -> musicServiceConnection.mediaBrowser.sendCustomAction(ACTION_SET_PLAYBACK_SPEED, bundleOf("playbackSpeed" to 1.25f), null)
					1.25f -> musicServiceConnection.mediaBrowser.sendCustomAction(ACTION_SET_PLAYBACK_SPEED, bundleOf("playbackSpeed" to 1.5f), null)
					1.5f -> musicServiceConnection.mediaBrowser.sendCustomAction(ACTION_SET_PLAYBACK_SPEED, bundleOf("playbackSpeed" to 2.0f), null)
					2.0f -> musicServiceConnection.mediaBrowser.sendCustomAction(ACTION_SET_PLAYBACK_SPEED, bundleOf("playbackSpeed" to 1.0f), null)
					else -> musicServiceConnection.mediaBrowser.sendCustomAction(ACTION_SET_PLAYBACK_SPEED, bundleOf("playbackSpeed" to 1.5f), null)
				}
			}
		} else {
			musicServiceConnection.mediaBrowser.sendCustomAction(ACTION_SET_PLAYBACK_SPEED, bundleOf("playbackSpeed" to 1.0f), null)
		}
	}

	override fun onCleared() {
		super.onCleared()
		musicServiceConnection.unsubscribe(
				MEDIA_ROOT_ID,
				object : MediaBrowserCompat.SubscriptionCallback() {})
	}
}