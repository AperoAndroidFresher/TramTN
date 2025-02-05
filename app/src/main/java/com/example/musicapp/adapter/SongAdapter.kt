package com.example.musicapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.musicapp.R
import com.example.musicapp.databinding.ItemSongBinding
import com.example.musicapp.databinding.ItemSongGridBinding
import com.example.musicapp.interfaces.OnSongClickListener
import com.example.musicapp.models.Song
import java.io.File

class SongAdapter(
    private var songs: MutableList<Song>,
    private val listener: OnSongClickListener,
    private var isGridLayout: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setLayoutType(isGrid: Boolean) {
        isGridLayout = isGrid
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridLayout) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            val binding = ItemSongGridBinding.inflate(layoutInflater, parent, false)
            GridSongViewHolder(binding)
        } else {
            val binding = ItemSongBinding.inflate(layoutInflater, parent, false)
            LinearSongViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song = songs[position]
        when (holder) {
            is GridSongViewHolder -> holder.bind(song, position)
            is LinearSongViewHolder -> holder.bind(song, position)
        }
    }

    override fun getItemCount(): Int = songs.size

    inner class GridSongViewHolder(private val binding: ItemSongGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song, position: Int) {
            binding.songTitle.text = song.title
            binding.songAuthor.text = song.artist
            setImage(binding, song)

            binding.root.setOnClickListener {
                Toast.makeText(binding.root.context, "Playing: ${song.title} by ${song.artist}", Toast.LENGTH_SHORT).show()
                listener.onSongClick(song)
            }

            binding.btnOption.setOnClickListener { view ->
                showPopupMenu(view, song, position)
            }
        }
    }

    inner class LinearSongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song, position: Int) {
            binding.songTitle.text = song.title
            binding.songAuthor.text = song.artist
            setImage(binding, song)

            binding.root.setOnClickListener {
                Toast.makeText(binding.root.context, "Playing: ${song.title} by ${song.artist}", Toast.LENGTH_SHORT).show()
                listener.onSongClick(song)
            }

            binding.btnOption.setOnClickListener { view ->
                showPopupMenu(view, song, position)
            }
        }
    }

    private fun setImage(binding: Any, song: Song) {
        val context = when (binding) {
            is ItemSongGridBinding -> binding.root.context
            is ItemSongBinding -> binding.root.context
            else -> return
        }

        val imageView = when (binding) {
            is ItemSongGridBinding -> binding.songImage
            is ItemSongBinding -> binding.songImage
            else -> return
        }

        val albumArtFile = File(song.albumArt ?: "")
        val albumArtUri = if (albumArtFile.exists()) song.albumArt else null

        Glide.with(context)
            .load(albumArtUri)
            .placeholder(R.drawable.img_default_music_art)
            .error(R.drawable.img_default_music_art)
            .override(100, 100)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    private fun showPopupMenu(view: View, song: Song, position: Int) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_playlist_options, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_rename -> {
                    showRenameDialog(view.context, song, position)
                    true
                }
                R.id.menu_remove -> {
                    removeSong(position)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showRenameDialog(context: Context, song: Song, position: Int) {
        val input = EditText(context)
        input.setText(song.title)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Rename Playlist")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    songs[position] = song.copy(title = newName)
                    notifyItemChanged(position)  // Cập nhật giao diện
                    Toast.makeText(context, "Playlist renamed to: $newName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Tên không được để trống!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun removeSong(position: Int) {
        songs.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, songs.size)
    }
}
