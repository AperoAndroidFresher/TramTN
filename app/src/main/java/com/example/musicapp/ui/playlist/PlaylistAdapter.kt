package com.example.musicapp.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.models.Playlist

class PlaylistAdapter(
    private val playlists: MutableList<Playlist>,
    private val onRename: (Playlist) -> Unit,
    private val onRemove: (Playlist) -> Unit,
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.playlistTitle)
        private val songCount: TextView = view.findViewById(R.id.songCount)
        private val btnMore: ImageButton = view.findViewById(R.id.btnMore)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPlaylistClick(playlists[position])
                }
            }

            btnMore.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(it, playlists[position], position)
                }
            }
        }

        fun bind(playlist: Playlist) {
            title.text = playlist.title
            songCount.text = "${playlist.songs.size} songs"
        }

        private fun showPopupMenu(view: View, playlist: Playlist, position: Int) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.menu_playlist_options, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_rename -> {
                        onRename(playlist)
                        true
                    }
                    R.id.menu_remove -> {
                        removePlaylist(position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }

    private fun removePlaylist(position: Int) {
        if (position in playlists.indices) {
            playlists.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, playlists.size)
        }
    }
}
