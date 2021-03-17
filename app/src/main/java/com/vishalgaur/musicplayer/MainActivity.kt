package com.vishalgaur.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//		setSupportActionBar(findViewById(R.id.my_toolbar))
//
//        @Inject
//        lateinit var glide: RequestManager

    }
}