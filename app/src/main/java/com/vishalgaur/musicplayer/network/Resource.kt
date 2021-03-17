package com.vishalgaur.musicplayer.network

import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat


const val NETWORK_ERROR = "NETWORK_ERROR"
const val MEDIA_ROOT_ID = "root_id"


//extending PlaybackStateCompat to merge states inline
inline val PlaybackStateCompat.isPrepared
	get() = state == PlaybackStateCompat.STATE_BUFFERING ||
			state == PlaybackStateCompat.STATE_PLAYING ||
			state == PlaybackStateCompat.STATE_PAUSED

inline val PlaybackStateCompat.isPlaying
	get() = state == PlaybackStateCompat.STATE_BUFFERING ||
			state == PlaybackStateCompat.STATE_PLAYING

inline val PlaybackStateCompat.isPlayEnabled
	get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
			(actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L &&
					state == PlaybackStateCompat.STATE_PAUSED)

inline val PlaybackStateCompat.currentPlaybackPosition: Long
	get() = if (state == PlaybackStateCompat.STATE_PLAYING) {
		val delTime = SystemClock.elapsedRealtime() - lastPositionUpdateTime
		(position + (delTime * playbackSpeed)).toLong()
	} else position

enum class Status { SUCCESS, LOADING, ERROR }

fun MediaMetadataCompat.toSong(): Song? {
	return description?.let {
		Song(
                it.mediaId ?: "",
                it.mediaUri.toString(),
                it.title.toString(),
                it.iconUri.toString(),
                ArrayList(it.subtitle.toString().split(",").map { name -> name.trim() })
        )
	}
}

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

	companion object {
		fun <T> success(data: T?) = Resource(Status.SUCCESS, data, null)

		fun <T> error(message: String, data: T?) = Resource(Status.ERROR, data, message)

		fun <T> loading(data: T?) = Resource(Status.LOADING, data, null)
	}
}