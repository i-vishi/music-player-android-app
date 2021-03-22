package com.vishalgaur.musicplayer.player

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import com.google.android.material.slider.Slider
import com.vishalgaur.musicplayer.MainViewModel
import com.vishalgaur.musicplayer.R
import com.vishalgaur.musicplayer.databinding.DialogSpeedSelectionBinding

private val speedMap = mapOf(
		0.25f to "Slowest (0.25x)",
		0.5f to "Slower (0.5x)",
		0.75f to "Slower (0.75x)",
		1.0f to "Normal",
		1.25f to "Faster (1.25x)",
		1.5f to "Faster (1.5x)",
		1.75f to "Faster (1.75x)",
		2.0f to "Fastest (2x)"
)

class SpeedSelectionDialogFragment(private val currPlaybackSpeed: Float?) : DialogFragment() {

	private lateinit var binding: DialogSpeedSelectionBinding
	private lateinit var mainViewModel: MainViewModel

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = DialogSpeedSelectionBinding.inflate(inflater)
		binding.lifecycleOwner = this

		mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

		return binding.root
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

		val builder = AlertDialog.Builder(activity)
		// Get the layout inflater
		val inflater = requireActivity().layoutInflater
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		val dialogView = inflater.inflate(R.layout.dialog_speed_selection, null)
		builder.setView(dialogView)

		val slider = dialogView.findViewById<Slider>(R.id.dialog_speed_slider)
		val speedText = dialogView.findViewById<TextView>(R.id.dialog_speed_text)
		speedText.text = speedMap[currPlaybackSpeed]
		if (currPlaybackSpeed != null) {
			slider.value = currPlaybackSpeed
		}

		slider.addOnChangeListener { _, value, fromUser ->
			if (fromUser) {
				speedText.text = speedMap[value]
			}
		}

		dialogView.findViewById<Button>(R.id.dialog_speed_btn_cancel).setOnClickListener {
			dialog?.cancel()
		}

		dialogView.findViewById<Button>(R.id.dialog_speed_btn_ok).setOnClickListener {
			mainViewModel.togglePlaybackSpeed(slider.value)
			dialog?.cancel()

		}

		return builder.create()

	}

	companion object {
		const val TAG = "SpeedSelectionDialog"
	}
}