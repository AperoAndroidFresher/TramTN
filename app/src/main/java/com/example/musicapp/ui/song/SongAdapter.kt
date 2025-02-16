package com.example.musicapp.ui.song

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.databinding.ItemSongBinding
import com.example.musicapp.databinding.ItemSongGridBinding
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.ui.playlist.PlaylistDialogFragment
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.data.local.repository.PlaylistRepository
import com.example.musicapp.ui.playlist.PlaylistViewModel
import com.example.musicapp.ui.playlist.PlaylistViewModelFactory

class SongAdapter(
    private var songs: MutableList<Song>,
    private val listener: OnSongClickListener,
    private var isGridLayout: Boolean,
    private val isInPlaylistFragment: Boolean,
    private val viewModel: PlaylistViewModel
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

        val menuRes = if (isInPlaylistFragment) {
            R.menu.menu_playlist_song_options
        } else {
            R.menu.menu_library_song_options
        }
        popupMenu.menuInflater.inflate(menuRes, popupMenu.menu)

        try {
            val fields = popupMenu.javaClass.getDeclaredField("mPopup")
            fields.isAccessible = true
            val menuHelper = fields.get(popupMenu)
            val classPopupMenu = Class.forName(menuHelper.javaClass.name)
            val setForceIcons = classPopupMenu.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            setForceIcons.invoke(menuHelper, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_remove_song -> {
                    val playlistId = 1
                    viewModel.removeSongFromPlaylist(playlistId, song.songId)
                    true
                }
                R.id.menu_add -> {
                    openPlaylistDialog(view.context, song)
                    true
                }
                R.id.menu_share -> {
                    shareSong(view.context, song)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun shareSong(context: Context, song: Song) {

        val songUri = Uri.parse(song.songUri)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, songUri)
            type = "audio/*"
        }

        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(shareIntent, "Share song"))
        } else {
            Toast.makeText(context, "No sharing apps available", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openPlaylistDialog(context: Context, song: Song) {
        val activity = context as? AppCompatActivity
        activity?.let {
            val playlistDialogFragment = PlaylistDialogFragment.newInstance(song)
            playlistDialogFragment.show(it.supportFragmentManager, "PlaylistDialogFragment")
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

}
