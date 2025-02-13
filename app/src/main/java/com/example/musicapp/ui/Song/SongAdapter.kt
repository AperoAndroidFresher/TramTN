package com.example.musicapp.ui.Song

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicapp.R
import com.example.musicapp.ui.main.MainActivity
import com.example.musicapp.databinding.ItemSongBinding
import com.example.musicapp.databinding.ItemSongGridBinding
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.ui.playlist.PlaylistDialogFragment
import com.example.musicapp.models.Song

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
            binding.songDuration.text = formatDuration(song.duration)
            setImage(binding, song)

            binding.root.setOnClickListener {
                Log.d("SongAdapter", "Click vào bài hát: ${song.title}")
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
            binding.songDuration.text = formatDuration(song.duration)
            setImage(binding, song)

            binding.root.setOnClickListener {
                Log.d("SongAdapter", "Click vào bài hát: ${song.title}")
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

            Glide.with(context)
                .load(song.albumArt)
                .error(R.drawable.img_default_music_art)
                .placeholder(R.drawable.img_default_music_art)
                .into(imageView)

    }

    private fun showPopupMenu(view: View, song: Song, position: Int) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_playlist_options, popupMenu.menu)

        try {
            val fields = popupMenu.javaClass.getDeclaredField("mPopup")
            fields.isAccessible = true
            val menuHelper = fields.get(popupMenu)
            val classPopupMenu = Class.forName(menuHelper.javaClass.name)
            val setForceIcons = classPopupMenu.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            setForceIcons.invoke(menuHelper, true)

            val contextWrapper = ContextThemeWrapper(view.context, R.style.CustomPopupMenu)
            val popupMenu = PopupMenu(contextWrapper, view)
            popupMenu.menuInflater.inflate(R.menu.menu_playlist_options, popupMenu.menu)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isGridLayout) {
            popupMenu.menu.findItem(R.id.menu_add).isVisible = false
            popupMenu.menu.findItem(R.id.menu_share).isVisible = false
        } else {
            popupMenu.menu.findItem(R.id.menu_rename).isVisible = false
            popupMenu.menu.findItem(R.id.menu_remove).isVisible = false
        }
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_rename -> {
//                    showRenameDialog(view.context, song, position)
                    true
                }
                R.id.menu_remove -> {
                    removeSong(position)
                    true
                }
                R.id.menu_add -> {
                    showPlaylistDialog(view.context, song)
                    true
                }
                R.id.menu_share -> true
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun showPlaylistDialog(context: Context, song: Song) {
        if (context is MainActivity) {
            val playlists = context.getPlaylists()
            val playlistDialog = PlaylistDialogFragment.newInstance(song)
            playlistDialog.show(context.supportFragmentManager, "PlaylistDialogFragment")
        }
    }


    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    private fun removeSong(position: Int) {
        songs.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, songs.size)
    }
}
