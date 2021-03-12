package com.vishalgaur.musicplayer.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.material.slider.Slider
import com.vishalgaur.musicplayer.MainViewModel
import com.vishalgaur.musicplayer.R
import com.vishalgaur.musicplayer.databinding.FragmentPlayerBinding
import com.vishalgaur.musicplayer.network.Song
import com.vishalgaur.musicplayer.network.Status
import com.vishalgaur.musicplayer.network.isPlaying
import com.vishalgaur.musicplayer.network.toSong
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "PlayerFragment"

@AndroidEntryPoint
class PlayerFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var binding: FragmentPlayerBinding

    private lateinit var mainViewModel: MainViewModel

    private val playerViewModel: PlayerViewModel by viewModels()

    private var currPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private var updateSlider = true

//    class PlayerViewModelFactory(private val songId: String) : ViewModelProvider.Factory {
//        @Suppress("UNCHECKED_CAST")
//        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
//                return PlayerViewModel(songId) as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView Starts")
        binding = FragmentPlayerBinding.inflate(inflater)
        binding.lifecycleOwner = this

        Log.d(TAG, "onCreateView ends")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated Starts")
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = playerViewModel

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        subscribeToObservers()

        binding.playerPlayButton.setOnClickListener {
            currPlayingSong?.let {
                mainViewModel.playOrPauseSong(it, true)
            }
        }

        binding.playerPrevButton.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.playerNextButton.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        binding.playerTimeSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                updateSlider = false
            }

            override fun onStopTrackingTouch(slider: Slider) {
                slider.let {
                    mainViewModel.seekSongTo(it.value.toLong())
                    updateSlider = true
                }
            }
        })

        binding.playerTimeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                setCurrTimeTextView(value.toLong())
            }
        }

        Log.d(TAG, "onViewCreated ends")
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (currPlayingSong == null && songs.isNotEmpty()) {
                                currPlayingSong = songs[0]
                                updatePlayerData(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.currPlayingSong.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            currPlayingSong = it.toSong()
            updatePlayerData(currPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            binding.playerPlayButton.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause_48 else R.drawable.ic_play_arrow_48
            )

            binding.playerTimeSlider.value = it?.position?.toFloat() ?: 0F
        }

        playerViewModel.currPlayerPosition.observe(viewLifecycleOwner) {
            if (updateSlider) {
                binding.playerTimeSlider.value = it.toFloat()
                setCurrTimeTextView(it)
            }
        }

        playerViewModel.currSongDuration.observe(viewLifecycleOwner) {
            binding.playerTimeSlider.valueTo = it.toFloat()
            val formattedTime = getMinSec(it)
            binding.totalTimeTextView.text = formattedTime
        }
    }

    private fun updatePlayerData(song: Song) {
        binding.playerSongNameView.text = song.title
        binding.playerArtistsView.text = song.artist.joinToString { name -> name }
        glide.load(song.banner).into(binding.playerImageView)
    }

    private fun setCurrTimeTextView(time: Long) {
        binding.currTimeTextView.text = getMinSec(time)
    }

    private fun getMinSec(ms: Long): String {
        return if (ms > 0) {
            val sec = ms / 1000
            "${sec / 60}:${sec % 60}"
        } else "00:00"
    }

}