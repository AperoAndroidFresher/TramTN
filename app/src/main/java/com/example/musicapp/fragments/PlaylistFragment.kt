package com.example.musicapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.activities.CreatePlaylistActivity
import com.example.musicapp.activities.PlaylistDetailActivity
import com.example.musicapp.adapters.PlaylistAdapter
import com.example.musicapp.models.Playlist
import com.example.musicapp.activities.CreatePlaylistActivity.Companion.playlists

class PlaylistFragment : Fragment() {

    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var emptyView: RelativeLayout
    private lateinit var addButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist, container, false)
        playlistRecyclerView = view.findViewById(R.id.playlistRecyclerView)
        emptyView = view.findViewById(R.id.empty_view)
        addButton = view.findViewById(R.id.add_playlist_button)

        addButton.setOnClickListener {
            val intent = Intent(requireContext(), CreatePlaylistActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        if (playlists.isEmpty()) {
            playlistRecyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            playlistRecyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            val adapter = PlaylistAdapter(playlists) { playlist ->
                val intent = Intent(requireContext(), PlaylistDetailActivity::class.java)
                intent.putExtra("PLAYLIST_ID", playlist.id)
                startActivity(intent)
            }
            playlistRecyclerView.adapter = adapter
        }
    }
}
