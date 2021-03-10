package com.vishalgaur.musicplayer.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vishalgaur.musicplayer.databinding.FragmentPlayerBinding

private const val TAG = "PlayerFragment"

class PlayerFragment : Fragment() {

    private lateinit var binding: FragmentPlayerBinding

    class PlayerViewModelFactory(private val songId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(songId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

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

        val songId = PlayerFragmentArgs.fromBundle(requireArguments()).songId

        Log.d(TAG, "SONG ID: $songId")

        val viewModelFactory = PlayerViewModelFactory(songId)
        binding.viewModel = ViewModelProvider(this, viewModelFactory).get(PlayerViewModel::class.java)
        Log.d(TAG, "onViewCreated ends")
    }

}