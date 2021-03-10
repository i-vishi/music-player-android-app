package com.vishalgaur.musicplayer.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vishalgaur.musicplayer.databinding.SongListItemBinding
import com.vishalgaur.musicplayer.network.Song

class SongItemAdapter : ListAdapter<Song, SongItemAdapter.ViewHolder>(DiffCallback) {

//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val songTitle: TextView = view.findViewById(R.id.song_card_title)
//        val songArtist: TextView = view.findViewById(R.id.song_card_artist)
//    }

    lateinit var onClickListener: OnClickListener

    inner class ViewHolder(private var binding: SongListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(songData: Song) {
            binding.song = songData
            binding.songCardTitle.text = songData.title
            binding.songCardArtist.text = songData.artist.joinToString { name -> name }

            binding.songCard.setOnClickListener {
                onClickListener.onClick(songData)
            }

            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.songId == newItem.songId
        }
    }

    interface OnClickListener {
        fun onClick(songData: Song)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SongListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songData = getItem(position)
        holder.bind(songData)
    }
}