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
import com.vishalgaur.musicplayer.MainViewModel
import com.vishalgaur.musicplayer.R
import com.vishalgaur.musicplayer.databinding.FragmentHomeBinding
import com.vishalgaur.musicplayer.exomusicplayer.MusicServiceConnection
import com.vishalgaur.musicplayer.network.Song
import com.vishalgaur.musicplayer.network.Status
import com.vishalgaur.musicplayer.player.PlayerViewModel

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    lateinit var mainViewModel: MainViewModel

    class HomeViewModelFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    class ViewModelFactory(private val musicServiceConnection: MusicServiceConnection) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(musicServiceConnection) as T
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


        Log.d(TAG, "onCreateView ends")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val musicServiceConnection = MusicServiceConnection(requireContext())
        val fac = ViewModelFactory(musicServiceConnection)
        mainViewModel = ViewModelProvider(this, fac).get(MainViewModel::class.java)

        val adapter = SongItemAdapter()

        adapter.onClickListener = object : SongItemAdapter.OnClickListener {
            override fun onClick(songData: Song) {
                mainViewModel.playOrPauseSong(songData)
//                viewModel.displaySelectedSong(songData.songId)
//                findNavController().navigate(HomeFragmentDirections.actionPlaySong(songData.songId))
//                viewModel.displaySelectedSongComplete()
            }
        }

        binding.songRecyclerView.adapter = adapter
    }

//    private fun subscribeToObservers() {
//        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
//            when(it.status) {
//                Status.SUCCESS -> {}
//                Status.LOADING ->
//                Status.ERROR -> Unit
//            }
//        }
//    }
}