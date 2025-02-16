package com.example.musicapp.ui.song

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentSongListBinding
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.data.local.entity.Song
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.ui.player.PlayerFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.data.local.entity.PlaylistSong
import com.example.musicapp.data.local.repository.PlaylistRepository
import com.example.musicapp.ui.playlist.PlaylistViewModel
import com.example.musicapp.ui.playlist.PlaylistViewModelFactory

class SongListFragment : Fragment(), OnSongClickListener {

    private var mediaPlayer: MediaPlayer? = null
    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!
    private lateinit var songAdapter: SongAdapter
    private var isGridLayout = false
    private var songList: MutableList<Song> = mutableListOf()
    private var playlistId: Int = -1

    private val viewModel: PlaylistViewModel by viewModels {
        PlaylistViewModelFactory(
            PlaylistRepository(
                AppDatabase.getDatabase(requireContext()).playlistDao(),
                AppDatabase.getDatabase(requireContext()).playlistSongDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = arguments?.getInt("playlistId") ?: -1
        songAdapter = SongAdapter(songList, this, isGridLayout)

        binding.recyclerView.layoutManager = getLayoutManager()
        binding.recyclerView.adapter = songAdapter

        loadSongsFromPlaylist(playlistId)

        setFragmentResultListener("sorting_result") { _, bundle ->
            bundle.getParcelableArrayList<Song>("sortedSongs")?.let { sortedSongs ->
                updatePlaylist(sortedSongs)
                viewModel.updatePlaylistSongsOrder(sortedSongs.mapIndexed { index, song ->
                    PlaylistSong(
                        playlistId = playlistId,
                        songId = song.songId,
                        orderIndexLinear = if (isGridLayout) 0 else index,
                        orderIndexGrid = if (isGridLayout) index else 0
                    )
                })
            }
        }

        binding.toggleLayoutButton.setOnClickListener {
            toggleLayout()
        }

        binding.btnSort.setOnClickListener {
            openSortingFragment()
        }
    }

    private fun loadSongsFromPlaylist(playlistId: Int) {
        lifecycleScope.launch {
            viewModel.getSongsByPlaylist(playlistId, isGridLayout).collectLatest { songs ->
                if (songList != songs) {
                    songList.clear()
                    songList.addAll(songs)
                    songAdapter.notifyDataSetChanged() // Thông báo adapter rằng dữ liệu đã thay đổi
                }
            }
        }
    }

    private fun toggleLayout() {
        isGridLayout = !isGridLayout
        binding.recyclerView.layoutManager = getLayoutManager()
        songAdapter.setLayoutType(isGridLayout)
        binding.toggleLayoutButton.setImageResource(
            if (isGridLayout) R.drawable.ic_grid_form else R.drawable.ic_linear_form
        )
        updateSongOrder()
    }

    private fun updateSongOrder() {
        val sortedSongs = if (isGridLayout) {
            songList.mapIndexed { index, song ->
                PlaylistSong(
                    playlistId = playlistId,
                    songId = song.songId,
                    orderIndexGrid = index
                )
            }
        } else {
            songList.mapIndexed { index, song ->
                PlaylistSong(
                    playlistId = playlistId,
                    songId = song.songId,
                    orderIndexLinear = index
                )
            }
        }

        viewModel.updatePlaylistSongsOrder(sortedSongs)
        updatePlaylist(songList)
    }


    private fun getLayoutManager(): RecyclerView.LayoutManager =
        if (isGridLayout) GridLayoutManager(requireContext(), 2)
        else LinearLayoutManager(requireContext())

    private fun updatePlaylist(sortedSongs: List<Song>) {
        songList.clear()
        songList.addAll(sortedSongs)
        songAdapter.notifyDataSetChanged()
    }

    private fun openSortingFragment() {
        val sortingFragment = SortingFragment().apply {
            arguments = Bundle().apply {
                putInt("playlistId", playlistId)
                putBoolean("isGridLayout", isGridLayout)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragContainer, sortingFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onSongClick(song: Song) {
        val playerFragment = PlayerFragment().apply {
            arguments = Bundle().apply {
                putParcelable("song", song)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragContainer, playerFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        _binding = null
    }
}

