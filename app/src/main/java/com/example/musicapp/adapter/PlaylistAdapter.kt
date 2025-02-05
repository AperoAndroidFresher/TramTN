package com.example.musicapp.adapters

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
        val title: TextView = view.findViewById(R.id.playlistTitle)
        val songCount: TextView = view.findViewById(R.id.songCount)
        val btnMore: ImageButton = view.findViewById(R.id.btnMore)

        fun updatePlaylists(newPlaylists: MutableList<Playlist>) {
            playlists.clear()
            playlists.addAll(newPlaylists)
            notifyDataSetChanged()
        }
        init {
            itemView.setOnClickListener {
                val playlist = playlists[adapterPosition]
                onPlaylistClick(playlist)
            }

            btnMore.setOnClickListener {
                val playlist = playlists[adapterPosition]
                onRename(playlist)
            }
        }


        fun bind(playlist: Playlist) {
            title.text = playlist.title
            songCount.text = "${playlist.songs.size} songs"

        }

        private fun showPopupMenu(view: View, playlist: Playlist) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.menu_playlist_options, popupMenu.menu)

            try {
                val fields = popupMenu.javaClass.getDeclaredFields()
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popupMenu)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceShowIcon =
                            classPopupHelper.getDeclaredMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
                        setForceShowIcon.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_rename -> {
                        onRename(playlist)
                        true
                    }
                    R.id.menu_remove -> {
                        onRemove(playlist)
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
}
