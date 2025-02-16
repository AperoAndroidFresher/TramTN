package com.example.musicapp.ui.playlist

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.databinding.FragmentPlaylistDialogBinding
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.data.local.repository.PlaylistRepository
import com.example.musicapp.ui.playlist.dialogs.CreatePlaylistDialog

class PlaylistDialogFragment : DialogFragment() {

    private var _binding: FragmentPlaylistDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var playlistAdapter: PlaylistAdapter
    private val playlists = mutableListOf<Playlist>()
    private var song: Song? = null
    private var listener: OnPlaylistSelectedListener? = null

    private val viewModel: PlaylistViewModel by viewModels {
        PlaylistViewModelFactory(
            PlaylistRepository(
                AppDatabase.getDatabase(requireContext()).playlistDao(),
                AppDatabase.getDatabase(requireContext()).playlistSongDao()
            )
        )
    }


    interface OnPlaylistSelectedListener {
        fun onPlaylistSelected(song: Song, playlist: Playlist)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            parentFragment is OnPlaylistSelectedListener -> parentFragment as OnPlaylistSelectedListener
            context is OnPlaylistSelectedListener -> context
            else -> null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        song = arguments?.getParcelable("song")
        val userId = getUserId()

        setupRecyclerView()
        observePlaylists()

        if (userId != -1) {
            viewModel.loadPlaylists(userId)
        }

        binding.btnAdd.setOnClickListener { showCreatePlaylistDialog(userId) }
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            playlists,
            viewModel,
            viewLifecycleOwner,
            onRename = { showRenameDialog(it) },
            onRemove = { deletePlaylist(it) },
            onPlaylistClick = { addSongToPlaylist(it) }
        )
        binding.recyclerViewPlaylists.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPlaylists.adapter = playlistAdapter
    }

    private fun observePlaylists() {
        viewModel.playlists.observe(viewLifecycleOwner) { newPlaylists ->
            playlists.clear()
            playlists.addAll(newPlaylists)
            playlistAdapter.notifyDataSetChanged()
            updateUI()
        }
    }

    private fun updateUI() {
        binding.viewFlipper.displayedChild = if (playlists.isEmpty()) 0 else 1
    }

    private fun showCreatePlaylistDialog(userId: Int) {
        if (userId == -1) return
        CreatePlaylistDialog(userId) { newPlaylist ->
            viewModel.addPlaylist(newPlaylist)
        }.show(parentFragmentManager, "CreatePlaylistDialog")
    }

    private fun showRenameDialog(playlist: Playlist) {
        val context = requireContext()
        val input = EditText(context).apply { setText(playlist.title) }

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Rename Playlist")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    viewModel.updatePlaylistTitle(playlist.playlistId, newTitle, getUserId())
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePlaylist(playlist: Playlist) {
        viewModel.deletePlaylist(playlist.playlistId, getUserId())
    }

    private fun getUserId(): Int {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getInt("userId", 0)
    }

    private fun addSongToPlaylist(playlist: Playlist) {
        song?.let { song ->
            viewModel.addSongToPlaylist(playlist.playlistId, song.songId)

            Toast.makeText(context, "Added to '${playlist.title}'", Toast.LENGTH_SHORT).show()
            listener?.onPlaylistSelected(song, playlist)
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.7).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance(song: Song): PlaylistDialogFragment {
            return PlaylistDialogFragment().apply {
                arguments = Bundle().apply { putParcelable("song", song) }
            }
        }
    }
}
