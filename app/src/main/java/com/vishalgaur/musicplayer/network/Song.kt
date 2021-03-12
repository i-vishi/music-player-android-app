package com.vishalgaur.musicplayer.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.ArrayList

data class Song(
    val songId: String = "",
    val url: String = "",
    val title: String = "",
    val banner: String = "",
    val artist: ArrayList<String> = ArrayList()
)