package com.example.musicapp.ui.song

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.data.local.entity.PlaylistSong
import com.example.musicapp.data.local.repository.PlaylistRepository
import com.example.musicapp.ui.main.MainActivity
import com.example.musicapp.ui.music.MusicService
import com.example.musicapp.ui.music.PlayerFragment
import com.example.musicapp.ui.playlist.PlaylistViewModel
import com.example.musicapp.ui.playlist.PlaylistViewModelFactory

class SongListFragment : Fragment(), OnSongClickListener {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter
    private var isGridLayout = false
    private var songList: MutableList<Song> = mutableListOf()
    private var playlistId: Int = -1

    private var musicService: MusicService? = null
    private var isBound = false

    private var selectedSongId: String? = null
    private var isPlaying: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            observeServiceState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

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

        songAdapter = SongAdapter(
            songs = songList,
            listener = this,
            isGridLayout = isGridLayout,
            isInPlaylistFragment = true,
            viewModel = viewModel
        )

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

    // Bind Service
    override fun onStart() {
        super.onStart()
        val intent = Intent(requireContext(), MusicService::class.java)
        // Đảm bảo service đang chạy foreground
        requireContext().startService(intent)
        // Bind service
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // Unbind Service
    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireContext().unbindService(connection)
            isBound = false
        }
    }

    private fun loadSongsFromPlaylist(playlistId: Int) {
        lifecycleScope.launch {
            viewModel.getSongsByPlaylist(playlistId, isGridLayout).collectLatest { songs ->
                songList.clear()
                songList.addAll(songs)
                songAdapter.notifyDataSetChanged()
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

    private fun getLayoutManager(): RecyclerView.LayoutManager =
        if (isGridLayout) GridLayoutManager(requireContext(), 2)
        else LinearLayoutManager(requireContext())

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
        if (!isBound || musicService == null) {
            Log.e("SongListFragment", "MusicService chưa bind!")
            return
        }

        Log.d("SongListFragment", "Người dùng chọn bài hát: ${song.title}")

        if (selectedSongId == null || selectedSongId != song.songId) {
            selectedSongId = song.songId
            isPlaying = true

            musicService?.playSong(song)
            Log.d("SongListFragment", "Gọi playSong(): ${song.title}")
            songAdapter.setSelectedSongId(song.songId)

            val playerFragment = PlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("SongList", ArrayList(songList))
                    putParcelable("SelectedSong", song)
                }
            }
            (activity as? MainActivity)?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragContainer, playerFragment)
                ?.addToBackStack(null)
                ?.commit()

            (activity as? OnSongClickListener)?.onSongClick(song)

        } else {
            isPlaying = !isPlaying
            if (isPlaying) {
                musicService?.resumeSong()
                Log.d("SongListFragment", "Gọi resumeSong()")
            } else {
                musicService?.pauseSong()
                Log.d("SongListFragment", "Gọi pauseSong()")
            }
        }
    }

    private fun observeServiceState() {
        musicService?.currentSong?.observe(viewLifecycleOwner) { song ->
            if (song != null) {
                selectedSongId = song.songId
                songAdapter.setSelectedSongId(song.songId)
            } else {
                selectedSongId = null
                songAdapter.setSelectedSongId(null)
            }
        }

        musicService?.isPlaying?.observe(viewLifecycleOwner) { playing ->
            isPlaying = playing

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

