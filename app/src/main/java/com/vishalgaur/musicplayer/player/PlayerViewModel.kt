package com.vishalgaur.musicplayer.player

import android.util.Log
import androidx.lifecycle.*
import com.vishalgaur.musicplayer.home.SongsApiStatus
import com.vishalgaur.musicplayer.network.Song
import com.vishalgaur.musicplayer.network.SongDatabase
import kotlinx.coroutines.launch
import java.lang.Exception


private const val TAG = "PlayerViewModel"

class PlayerViewModel(songId: Long) : ViewModel() {

    private val _selectedSongId = MutableLiveData<Long>()
    val selectedSongId: LiveData<Long> get() = _selectedSongId

    private val _status = MutableLiveData<SongsApiStatus>()
    val status: LiveData<SongsApiStatus> get() = _status

    private val _songData = MutableLiveData<Song?>()
    val songData: LiveData<Song?> get() = _songData

    init {
        _selectedSongId.value = songId
        getSongData()
    }

    private fun getSongData() {
        viewModelScope.launch {
            _status.value = SongsApiStatus.LOADING
            Log.d(TAG, "getting song data with id ${selectedSongId.value}")
            SongDatabase().collectionSongs.whereEqualTo("songId", selectedSongId.value).get()
                .addOnSuccessListener { result ->
                    try {
                        for (document in result) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                            _songData.value = document.toObject(Song::class.java)

                        }
                        _status.value = SongsApiStatus.DONE
                    } catch (err: Exception) {
                        Log.e(TAG, err.toString())
                        _songData.value = null
                    }

                }.addOnFailureListener { e ->
                    _status.value = SongsApiStatus.ERROR
                    _songData.value = null
                    Log.e(TAG, "Error getting Data", e)
                }
        }
    }

    val getArtists = Transformations.map(songData) {
        it?.artist?.joinToString { name -> name }
    }
}