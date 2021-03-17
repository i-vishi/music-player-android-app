package com.vishalgaur.musicplayer.network

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.lang.Exception

private const val SONG_COLLECTION = "songs"
private const val TAG = "SongDatabase"

class SongDatabase {

	private val firestore = FirebaseFirestore.getInstance()
	val collectionSongs = firestore.collection(SONG_COLLECTION)

	suspend fun getSongs(): List<Song> {
		return try {
			Log.d(TAG, "Getting all songs")
			collectionSongs.get().await().toObjects(Song::class.java)
		} catch (e: Exception) {
			emptyList()
		}
	}
}