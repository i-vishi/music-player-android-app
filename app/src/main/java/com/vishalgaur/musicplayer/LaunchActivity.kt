package com.vishalgaur.musicplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

private const val TIME_OUT: Long = 1500

class LaunchActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.layout_launch)
		loadLaunchScreen()
	}

	private fun loadLaunchScreen() {
		Handler(Looper.myLooper()!!).postDelayed({
			val intent = Intent(this, MainActivity::class.java)
			startActivity(intent)
			finish()
		}, TIME_OUT)
	}
}