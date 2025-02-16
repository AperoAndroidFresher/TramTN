package com.example.musicapp.ui.song

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.example.musicapp.databinding.FragmentSortingBinding
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.data.local.repository.PlaylistRepository
import com.example.musicapp.data.local.database.AppDatabase
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.musicapp.data.local.entity.PlaylistSong
import com.example.musicapp.ui.playlist.PlaylistViewModel
import com.example.musicapp.ui.playlist.PlaylistViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log
import java.util.Collections

class SortingFragment : Fragment() {

    private var _binding: FragmentSortingBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter
    private val songs: MutableList<Song> = mutableListOf()
    private var playlistId: Int = -1
    private var isGridLayout: Boolean = false

    private val viewModel: PlaylistViewModel by viewModels {
        PlaylistViewModelFactory(
            PlaylistRepository(
                AppDatabase.getDatabase(requireContext()).playlistDao(),
                AppDatabase.getDatabase(requireContext()).playlistSongDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSortingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = arguments?.getInt("playlistId", -1) ?: -1
        isGridLayout = arguments?.getBoolean("isGridLayout", false) ?: false

        Log.d("SortingFragment", "Received playlistId: $playlistId, isGridLayout: $isGridLayout")

        if (playlistId == -1) {
            Toast.makeText(requireContext(), "Lỗi: Playlist không hợp lệ", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // Set up the RecyclerView with appropriate layout manager
        binding.recyclerView.layoutManager = if (isGridLayout) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }

        setupRecyclerView()
        loadSongsFromPlaylist()

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnConfirm.setOnClickListener {
            confirmSorting()
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(songs, object : OnSongClickListener {
            override fun onSongClick(song: Song) {
                Toast.makeText(requireContext(), "Đã chọn bài hát: ${song.title}", Toast.LENGTH_SHORT).show()
            }
        }, isGridLayout)

        binding.recyclerView.adapter = songAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                    (if (isGridLayout) ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT else 0), 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                // Swap the songs in the list
                Collections.swap(songs, fromPosition, toPosition)
                songAdapter.notifyItemMoved(fromPosition, toPosition)

                // Optionally, update the order of songs in the database if needed
                updateSongOrderInDatabase()

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun loadSongsFromPlaylist() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getSongsByPlaylist(playlistId, isGridLayout).collectLatest { songList ->
                Log.d("SortingFragment", "Songs loaded: ${songList.size}")

                if (songs != songList) {
                    songs.clear()
                    songs.addAll(songList)
                    songAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun confirmSorting() {
        if (songs.isEmpty()) {
            Toast.makeText(requireContext(), "Không có bài hát nào để sắp xếp", Toast.LENGTH_SHORT).show()
            return
        }

        val sortedPlaylistSongs = songs.mapIndexed { index, song ->
            PlaylistSong(
                playlistId = playlistId,
                songId = song.songId,
                orderIndexLinear = if (isGridLayout) 0 else index,
                orderIndexGrid = if (isGridLayout) index else 0
            )
        }

        // Update the song order in the database
        viewModel.updatePlaylistSongsOrder(sortedPlaylistSongs)
        Toast.makeText(requireContext(), "Sắp xếp thành công", Toast.LENGTH_SHORT).show()

        // Return to the previous fragment
        parentFragmentManager.popBackStack()
    }

    private fun updateSongOrderInDatabase() {

        val sortedPlaylistSongs = songs.mapIndexed { index, song ->
            PlaylistSong(
                playlistId = playlistId,
                songId = song.songId,
                orderIndexLinear = if (isGridLayout) 0 else index,
                orderIndexGrid = if (isGridLayout) index else 0
            )
        }

        viewModel.updatePlaylistSongsOrder(sortedPlaylistSongs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
