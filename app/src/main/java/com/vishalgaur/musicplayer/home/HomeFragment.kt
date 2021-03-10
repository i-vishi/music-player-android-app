package com.vishalgaur.musicplayer.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.vishalgaur.musicplayer.R
import com.vishalgaur.musicplayer.databinding.FragmentHomeBinding
import com.vishalgaur.musicplayer.network.Song

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    class HomeViewModelFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView starts")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val adapter = SongItemAdapter()

        adapter.onClickListener = object : SongItemAdapter.OnClickListener {
            override fun onClick(songData: Song) {
                viewModel.displaySelectedSong(songData.songId)
                findNavController().navigate(HomeFragmentDirections.actionPlaySong(songData.songId))
                viewModel.displaySelectedSongComplete()
            }
        }

        binding.songRecyclerView.adapter = adapter
        Log.d(TAG, "onCreateView ends")
        return binding.root
    }
}