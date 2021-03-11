package com.vishalgaur.musicplayer.network

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.lang.Exception

private const val SONG_COLLECTION = "songs"
private const val TAG = "SongDatabase"

class SongDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    val collectionSongs = firestore.collection(SONG_COLLECTION)

    fun getSongs(): List<Song> {
        var res = mutableListOf<Song>()
        SongDatabase().collectionSongs.get().addOnSuccessListener { result ->
            try {
                res = result.toObjects(Song::class.java)
            } catch (err: Exception) {
                Log.e(TAG, err.toString())
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error getting Data: ", e)
        }
        return res
    }
}