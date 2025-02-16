package com.example.musicapp.ui.song

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.databinding.ItemSongBinding
import com.example.musicapp.databinding.ItemSongGridBinding
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.ui.playlist.PlaylistDialogFragment
import com.example.musicapp.data.local.entity.Song

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
        popupMenu.menuInflater.inflate(R.menu.menu_playlist_options, popupMenu.menu)

        // Buộc biểu tượng hiển thị trong PopupMenu
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

        // Ẩn/hiển thị các mục menu theo layout
        if (isGridLayout) {
            popupMenu.menu.findItem(R.id.menu_add).isVisible = false
            popupMenu.menu.findItem(R.id.menu_share).isVisible = false
        } else {
            popupMenu.menu.findItem(R.id.menu_rename).isVisible = false
            popupMenu.menu.findItem(R.id.menu_remove).isVisible = false
        }

        // Xử lý các sự kiện trên menu
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_rename -> {
                    // Chưa triển khai showRenameDialog
                    true
                }
                R.id.menu_remove -> {
                    removeSong(position)
                    true
                }
                R.id.menu_add -> {
                    openPlaylistDialog(view.context, song)
                    true
                }
                R.id.menu_share -> {
                    // Triển khai chia sẻ nếu cần
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
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

    private fun removeSong(position: Int) {
        songs.removeAt(position)
        notifyItemRemoved(position)
    }
}
