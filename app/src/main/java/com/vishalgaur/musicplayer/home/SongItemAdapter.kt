package com.vishalgaur.musicplayer.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.vishalgaur.musicplayer.R
import com.vishalgaur.musicplayer.network.Song
import javax.inject.Inject

class SongItemAdapter @Inject constructor(private val glide: RequestManager) :
		RecyclerView.Adapter<SongItemAdapter.ViewHolder>() {

	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val songTitle: TextView = view.findViewById(R.id.song_card_title)
		val songArtist: TextView = view.findViewById(R.id.song_card_artist)
		val songAlbumArt: ImageView = view.findViewById(R.id.song_card_image_view)
	}

	private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
		override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
			return oldItem.songId == newItem.songId
		}

		override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
			return oldItem.hashCode() == newItem.hashCode()
		}
	}

	private val differ = AsyncListDiffer(this, diffCallback)

	var songs: List<Song>
		get() = differ.currentList
		set(value) = differ.submitList(value)

//    lateinit var onClickListener: OnClickListener

//    inner class ViewHolder(private var binding: SongListItemBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(songData: Song) {
//            binding.song = songData
//            binding.songCardTitle.text = songData.title
//            binding.songCardArtist.text = songData.artist.joinToString { name -> name }
//
//            binding.songCard.setOnClickListener {
//                onClickListener.onClick(songData)
//            }
//
//            binding.executePendingBindings()
//        }
//    }

//    companion object DiffCallback : DiffUtil.ItemCallback<Song>() {
//        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
//            return oldItem === newItem
//        }
//
//        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
//            return oldItem.songId == newItem.songId
//        }
//    }

//    interface OnClickListener {
//        fun onClick(songData: Song)
//    }

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.song_list_item, parent, false)
        )
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val songData = songs[position]
		holder.itemView.apply {
			holder.songTitle.text = songData.title
			holder.songArtist.text = songData.artist.joinToString { name -> name }
			glide.load(songData.banner).into(holder.songAlbumArt)

			setOnClickListener {
				onItemClickListener?.let { click ->
					click(songData)
				}
			}
		}
	}

	private var onItemClickListener: ((Song) -> Unit)? = null

	fun setOnItemClickListener(listener: (Song) -> Unit) {
		onItemClickListener = listener
	}

	override fun getItemCount() = songs.size
}