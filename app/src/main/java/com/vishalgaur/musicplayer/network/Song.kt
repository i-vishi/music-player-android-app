package com.vishalgaur.musicplayer.network

import java.util.ArrayList

data class Song(
		val songId: String = "",
		val url: String = "",
		val title: String = "",
		val banner: String = "",
		val artist: ArrayList<String> = ArrayList()
)