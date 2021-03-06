package com.vishalgaur.musicplayer.player

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.vishalgaur.musicplayer.MainViewModel
import com.vishalgaur.musicplayer.R
import com.vishalgaur.musicplayer.databinding.FragmentPlayerBinding
import com.vishalgaur.musicplayer.network.Song
import com.vishalgaur.musicplayer.network.Status
import com.vishalgaur.musicplayer.network.isPlaying
import com.vishalgaur.musicplayer.network.toSong
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

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

	private var shuffleState: Int? = null
	private var repeatState: Int? = null
	private var playbackSpeed: Float? = null

	private var updateSlider = true

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

		binding.playerTimeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onStartTrackingTouch(seekBar: SeekBar?) {
				updateSlider = false
			}

			override fun onStopTrackingTouch(seekBar: SeekBar?) {
				seekBar?.let {
					mainViewModel.seekSongTo(it.progress.toLong())
					updateSlider = true
				}
			}

			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				if (fromUser) {
					setCurrTimeTextView(progress.toLong())
				}
			}
		})

		binding.playerShuffleButton.setOnClickListener {
			mainViewModel.toggleShuffleState()
		}

		binding.playerLoopButton.setOnClickListener {
			mainViewModel.toggleRepeatState()
		}

		binding.playerSpeedTextView.setOnClickListener {
			showSpeedDialog()
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

			binding.playerTimeSlider.progress = it?.position?.toInt() ?: 0
		}

		mainViewModel.shuffleState.observe(viewLifecycleOwner) {
			shuffleState = it
			binding.playerShuffleButton.setImageResource(
					if (shuffleState == 1) R.drawable.ic_shuffle_on_24 else R.drawable.ic_shuffle_24
			)
		}

		mainViewModel.repeatState.observe(viewLifecycleOwner) {
			repeatState = it
			binding.playerLoopButton.setImageResource(
					when (repeatState) {
						1 -> R.drawable.ic_repeat_one_on_24
						2 -> R.drawable.ic_repeat_on_24
						else -> R.drawable.ic_repeat_24
					}
			)
		}

		mainViewModel.playbackSpeed.observe(viewLifecycleOwner) {
			playbackSpeed = it
			binding.playerSpeedTextView.text = when (playbackSpeed) {
				1.0f -> getString(R.string.play_speed, playbackSpeed!!.roundToInt().toString())
				else -> getString(R.string.play_speed, playbackSpeed.toString())
			}
		}

		playerViewModel.currPlayerPosition.observe(viewLifecycleOwner) {
			if (updateSlider) {
				binding.playerTimeSlider.progress = it.toInt()
				setCurrTimeTextView(it)
			}
		}

		playerViewModel.currSongDuration.observe(viewLifecycleOwner) {
			binding.playerTimeSlider.max = it.toInt()
			val formattedTime = getMinSec(it)
			binding.totalTimeTextView.text = formattedTime
		}
	}

	private fun showSpeedDialog() {
		val speedSelectionDialogFragment = SpeedSelectionDialogFragment(playbackSpeed)
		speedSelectionDialogFragment.show(childFragmentManager, SpeedSelectionDialogFragment.TAG)
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
			val mn = sec / 60
			val sc = sec % 60
			val strMn = if (mn < 10) "0$mn" else "$mn"
			val strSc = if (sc < 10) "0$sc" else "$sc"
			"$strMn:$strSc"
		} else "00:00"
	}

}