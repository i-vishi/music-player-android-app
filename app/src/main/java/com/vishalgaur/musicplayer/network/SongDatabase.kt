package com.vishalgaur.musicplayer.network

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.lang.Exception

private const val SONG_COLLECTION = "songs"

class SongDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    val collectionSongs = firestore.collection(SONG_COLLECTION)

//    fun getSongs(): List<Song> {
//        return try {
//            collectionSongs.get().result!!.toObjects(Song::class.java)
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
}