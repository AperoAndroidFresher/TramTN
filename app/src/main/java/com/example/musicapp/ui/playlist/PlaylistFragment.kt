package com.example.musicapp.ui.playlist

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.databinding.FragmentPlaylistBinding
import com.example.musicapp.ui.playlist.dialogs.CreatePlaylistDialog
import com.example.musicapp.ui.playlist.dialogs.showRenameDialog
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.repository.PlaylistRepository
import com.example.musicapp.ui.song.SongListFragment

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PlaylistAdapter
    private val playlists = mutableListOf<Playlist>()
    private var userId: Int = 0

    private val viewModel: PlaylistViewModel by viewModels {
        PlaylistViewModelFactory(
            PlaylistRepository(
                AppDatabase.getDatabase(requireContext()).playlistDao(),
                AppDatabase.getDatabase(requireContext()).playlistSongDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        viewModel.loadPlaylists(userId)

        adapter = PlaylistAdapter(
            playlists,
            viewModel,
            viewLifecycleOwner,
            onRename = { playlist ->
                showRenameDialog(requireContext(), playlist) { newTitle ->
                    viewModel.updatePlaylist(playlist.copy(title = newTitle))
                }
            },
            onRemove = { playlist ->
                viewModel.deletePlaylist(playlist.playlistId, userId)
            },
            onPlaylistClick = { playlist ->
                val songListFragment = SongListFragment().apply {
                    arguments = Bundle().apply {
                        putInt("playlistId", playlist.playlistId)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragContainer, songListFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.rvPlaylist.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaylist.adapter = adapter

        binding.btnAddPlaylist.setOnClickListener {
            val dialog = CreatePlaylistDialog(
                userId = userId,
                onCreate = { newPlaylist ->
                    viewModel.addPlaylist(newPlaylist)
                }
            )
            dialog.show(parentFragmentManager, "CreatePlaylistDialog")
        }
        binding.btnAdd.setOnClickListener {
            val dialog = CreatePlaylistDialog(
                userId = userId,
                onCreate = { newPlaylist ->
                    viewModel.addPlaylist(newPlaylist)
                }
            )
            dialog.show(parentFragmentManager, "CreatePlaylistDialog")
        }
        viewModel.playlists.observe(viewLifecycleOwner) { newPlaylists ->
            updatePlaylists(newPlaylists)
        }
    }

    private fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        adapter.notifyDataSetChanged()
        updateUI()
    }

    private fun updateUI() {
        if (playlists.isEmpty()) {
            binding.title.visibility = View.GONE
            binding.tvNoPlaylist.visibility = View.VISIBLE
            binding.rvPlaylist.visibility = View.GONE
            binding.btnAddPlaylist.visibility = View.VISIBLE
        } else {
            binding.title.visibility = View.VISIBLE
            binding.btnAddPlaylist.visibility = View.GONE
            binding.tvNoPlaylist.visibility = View.GONE
            binding.rvPlaylist.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
