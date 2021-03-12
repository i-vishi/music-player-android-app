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
import androidx.recyclerview.widget.LinearLayoutManager
import com.vishalgaur.musicplayer.MainViewModel
import com.vishalgaur.musicplayer.R
import com.vishalgaur.musicplayer.databinding.FragmentHomeBinding
import com.vishalgaur.musicplayer.exomusicplayer.MusicServiceConnection
import com.vishalgaur.musicplayer.network.Song
import com.vishalgaur.musicplayer.network.Status
import com.vishalgaur.musicplayer.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var adapter: SongItemAdapter

//    class HomeViewModelFactory : ViewModelProvider.Factory {
//        @Suppress("UNCHECKED_CAST")
//        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
//                return HomeViewModel() as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
//
//    private val viewModel: HomeViewModel by viewModels {
//        HomeViewModelFactory()
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView starts")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this
//        binding.viewModel = viewModel


        Log.d(TAG, "onCreateView ends")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated starts")

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel

        binding.songRecyclerView.adapter = adapter
        binding.songRecyclerView.layoutManager = LinearLayoutManager(requireContext())

//        adapter = SongItemAdapter()

        subscribeToObservers()
        adapter.setOnItemClickListener {
            mainViewModel.playOrPauseSong(it)
        }

//        adapter.onClickListener = object : SongItemAdapter.OnClickListener {
//            override fun onClick(songData: Song) {
//                mainViewModel.playOrPauseSong(songData)
////                viewModel.displaySelectedSong(songData.songId)
////                findNavController().navigate(HomeFragmentDirections.actionPlaySong(songData.songId))
////                viewModel.displaySelectedSongComplete()
//            }
//        }

        Log.d(TAG, "onViewCreated ends")

    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        adapter.songs = songs
                    }
                }
                Status.LOADING -> {
                }
                Status.ERROR -> Unit
            }
        }
    }
}