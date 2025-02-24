package com.example.musicapp.ui.library

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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.data.local.entity.PlaylistSong
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.data.local.repository.PlaylistRepository
import com.example.musicapp.data.local.repository.SongRepository
import com.example.musicapp.data.remote.library.ApiClient
import com.example.musicapp.databinding.FragmentLibraryBinding
import com.example.musicapp.ui.music.MusicService
import com.example.musicapp.ui.playlist.PlaylistViewModel
import com.example.musicapp.ui.playlist.PlaylistViewModelFactory
import com.example.musicapp.ui.song.SongAdapter
import com.example.musicapp.ui.song.SongViewModel
import com.example.musicapp.ui.song.SongViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter
    private val songList = mutableListOf<Song>()
    private var isLocalSelected = true

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

    private val songViewModel: SongViewModel by viewModels {
        val songDao by lazy { AppDatabase.getDatabase(requireContext()).songDao() }
        val repository by lazy { SongRepository(songDao) }
        SongViewModelFactory(repository)
    }

    private val playlistViewModel: PlaylistViewModel by viewModels {
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
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireContext(), MusicService::class.java)
        requireContext().startService(intent)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }


    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireContext().unbindService(connection)
            isBound = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        syncMusic()

        binding.btnLocal.setOnClickListener {
            if (!isLocalSelected) {
                isLocalSelected = true
                updateTabUI()
                loadSongsFromDB()
            }
        }

        binding.btnRemote.setOnClickListener {
            if (isLocalSelected) {
                isLocalSelected = false
                updateTabUI()
                fetchRemoteSongs()
            }
        }

        binding.btnRetry.setOnClickListener {
            binding.recyclerView.visibility = View.VISIBLE
            binding.layoutError.visibility = View.GONE
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun syncMusic() {
        songViewModel.syncMusic(requireContext())
        loadSongsFromDB()
    }

    private fun loadSongsFromDB() {
        lifecycleScope.launch {
            songViewModel.allSongs.collect { songs ->
                songList.clear()
                songList.addAll(songs)
                songAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun fetchRemoteSongs() {
        binding.recyclerView.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val response = withContext(Dispatchers.IO) {
                    ApiClient.build().getSongs()
                }
                Log.d("LibraryFragment", "Response: $response")

                songList.clear()
                songList.addAll(response.map { remote ->
                    Song(
                        songId = UUID.randomUUID().toString(),
                        title = remote.title,
                        artist = remote.artist,
                        songUri = remote.path,
                        albumArt = "",
                        duration = remote.duration
                    )
                })
                withContext(Dispatchers.Main) {
                    if (songList.isNotEmpty()) {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.layoutError.visibility = View.GONE
                    } else {
                        binding.layoutError.visibility = View.VISIBLE
                    }
                    songAdapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.e("LibraryFragment", "Lỗi gọi API: ${e.message}", e)
                binding.layoutError.visibility = View.VISIBLE
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateTabUI() {
        val selectedColor = resources.getColor(R.color.teal_700)
        val unselectedColor = resources.getColor(R.color.gray)

        binding.btnLocal.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (isLocalSelected) selectedColor else unselectedColor)
        binding.btnRemote.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (isLocalSelected) unselectedColor else selectedColor)
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = songList,
            listener = object : OnSongClickListener {
                override fun onSongClick(song: Song) {
                    Log.d("LibraryFragment", "onSongClick: ${song.title}")
                    if (!isBound || musicService == null) return
                    if (selectedSongId == null || selectedSongId != song.songId) {
                        selectedSongId = song.songId
                        isPlaying = true
                        musicService?.playSong(song)

                        songAdapter.setSelectedSongId(song.songId)

                    } else {
                        isPlaying = !isPlaying
                        if (isPlaying) {
                            musicService?.resumeSong()
                        } else {
                            musicService?.pauseSong()
                        }
                    }
                }
            },
            isGridLayout = false,
            isInPlaylistFragment = false,
            viewModel = playlistViewModel
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = songAdapter
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
