package com.vishalgaur.musicplayer.player

import androidx.lifecycle.*
import com.vishalgaur.musicplayer.exomusicplayer.MusicService
import com.vishalgaur.musicplayer.exomusicplayer.MusicServiceConnection
import com.vishalgaur.musicplayer.network.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PlayerViewModel"

@HiltViewModel
class PlayerViewModel @Inject constructor(private val musicServiceConnection: MusicServiceConnection) :
    ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _currSongDuration = MutableLiveData<Long>()
    val currSongDuration: LiveData<Long> get() = _currSongDuration

    private val _currPlayerPosition = MutableLiveData<Long>()
    val currPlayerPosition: LiveData<Long> get() = _currPlayerPosition

    init {
        updatePlayerPosition()
    }

    private fun updatePlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val position = playbackState.value?.currentPlaybackPosition
                if (currPlayerPosition.value != position) {
                    _currPlayerPosition.value = position!!
                    _currSongDuration.value = MusicService.currSongDuration
                }
                delay(100L)
            }
        }
    }
}