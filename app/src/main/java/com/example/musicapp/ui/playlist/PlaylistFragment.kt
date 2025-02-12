package com.example.musicapp.ui.playlist

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.ui.playlist.dialogs.CreatePlaylistDialog
import com.example.musicapp.ui.playlist.dialogs.showRenameDialog
import com.example.musicapp.ui.Song.SongListFragment
import com.example.musicapp.models.Playlist

class PlaylistFragment : Fragment() {

    private lateinit var emptyView: TextView
    private lateinit var addButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var titleTextView: TextView
    private lateinit var adapter: PlaylistAdapter

    private val playlists = mutableListOf<Playlist>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyView = view.findViewById(R.id.tvNoPlaylist)
        addButton = view.findViewById(R.id.btnAddPlaylist)
        recyclerView = view.findViewById(R.id.rvPlaylist)
        titleTextView = view.findViewById(R.id.title)

        adapter = PlaylistAdapter(
            playlists,
            onRename = { playlist ->
                showRenameDialog(requireContext(), playlist) { newTitle ->
                    val index = playlists.indexOf(playlist)
                    if (index != -1) {
                        playlists[index].title = newTitle
                        adapter.notifyItemChanged(index)
                    }
                }
            },
            onRemove = { playlist ->
                playlists.remove(playlist)
                adapter.notifyDataSetChanged()
                updateUI()
            },
            onPlaylistClick = { playlist ->
                onPlaylistClick(playlist)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter


        addButton.setOnClickListener {
            val dialog = CreatePlaylistDialog { newPlaylist ->
                playlists.add(newPlaylist)
                adapter.notifyItemInserted(playlists.size - 1)
                updateUI()
            }
            dialog.show(parentFragmentManager, "CreatePlaylistDialog")
        }
        updateUI()

    }

    private fun updateUI() {
        if (playlists.isEmpty()) {
            titleTextView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            addButton.visibility = View.VISIBLE
        } else {
            titleTextView.visibility = View.VISIBLE
            addButton.visibility = View.GONE
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
    private fun onPlaylistClick(playlist: Playlist) {
        val songListFragment = SongListFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("songs", ArrayList(playlist.songs))
            }
        }

        val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
        transaction.replace(R.id.fragContainer, songListFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        adapter.notifyDataSetChanged()
        updateUI()
    }


}
