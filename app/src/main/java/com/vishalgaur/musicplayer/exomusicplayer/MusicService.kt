package com.vishalgaur.musicplayer.exomusicplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.vishalgaur.musicplayer.network.MEDIA_ROOT_ID
import com.vishalgaur.musicplayer.network.NETWORK_ERROR
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var defaultDataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var musicSource: MusicSource

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    var isForegroundService = false
    private lateinit var musicNotificationManager: MusicNotificationManager

    private var isPlayerInitialized = false

    private var currentPlayingSong: MediaMetadataCompat? = null

    companion object {
        var currSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            musicSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicNotificationListener(this)
        ) {

            // exoplayer returns negative value sometimes =====> added static duration to handle it
            currSongDuration = if (exoPlayer.duration > 0) exoPlayer.duration else 1001
        }


        val musicPlaybackPreparer = MusicPlaybackPreparer(musicSource) {
            currentPlayingSong = it
            preparePlayer(musicSource.songs, it, true)
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)

        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)

        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())

        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)

        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = musicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(musicSource.asMediaItems())
                        if (!isPlayerInitialized && musicSource.songs.isNotEmpty()) {
                            preparePlayer(musicSource.songs, musicSource.songs[0], false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(musicPlayerEventListener)

        exoPlayer.release()
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return musicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToBePlayed: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currSongIndex = if (currentPlayingSong == null) 0 else songs.indexOf(itemToBePlayed)
        exoPlayer.prepare(musicSource.asMediaSource(defaultDataSourceFactory))        // deprecated    use setMediaSource and prepare() instead
//        exoPlayer.setMediaSource(musicSource.asMediaSource(defaultDataSourceFactory))
//        exoPlayer.prepare()
        exoPlayer.seekTo(currSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

}