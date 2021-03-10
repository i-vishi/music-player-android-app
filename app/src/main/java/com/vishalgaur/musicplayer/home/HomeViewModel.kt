package com.vishalgaur.musicplayer.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishalgaur.musicplayer.network.Song
import com.vishalgaur.musicplayer.network.SongDatabase
import kotlinx.coroutines.launch
import java.lang.Exception

private const val TAG = "HomeViewModel"

enum class SongsApiStatus { LOADING, ERROR, DONE }

class HomeViewModel : ViewModel() {

    private val _status = MutableLiveData<SongsApiStatus>()
    val status: LiveData<SongsApiStatus> get() = _status

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val _navigateToSelectedSong = MutableLiveData<Long?>()
    val navigateToSelectedSong: LiveData<Long?> get() = _navigateToSelectedSong


    init {
        getAllSongs()
    }

    private fun getAllSongs() {
        viewModelScope.launch {
            _status.value = SongsApiStatus.LOADING
            var res: MutableList<Song>
            SongDatabase().collectionSongs.get().addOnSuccessListener { result ->
                try {
                    for (document in result) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                    res = result.toObjects(Song::class.java)
                    Log.d(TAG, res.toString())
                    _songs.value = res
                    _status.value = SongsApiStatus.DONE
                } catch (err: Exception) {
                    Log.e(TAG, err.toString())
                }

            }.addOnFailureListener { e ->
                _status.value = SongsApiStatus.ERROR
                Log.e(TAG, "Error getting Data", e)
            }
        }
    }

    fun displaySelectedSong(songId: Long) {
        _navigateToSelectedSong.value = songId
    }

    fun displaySelectedSongComplete() {
        _navigateToSelectedSong.value = null
    }
}