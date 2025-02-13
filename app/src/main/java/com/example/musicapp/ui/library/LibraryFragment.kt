package com.example.musicapp.ui.library

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.ui.Song.SongAdapter
import com.example.musicapp.databinding.FragmentLibraryBinding
import com.example.musicapp.models.Song
import com.example.musicapp.utils.SongUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.data.remote.ApiClient
import com.example.musicapp.ui.player.PlayerFragment
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

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadSongs(requireContext())

        binding.btnLocal.setOnClickListener {
            if (!isLocalSelected) {
                isLocalSelected = true
                updateTabUI()
                loadSongs(requireContext())
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
                        id = UUID.randomUUID().toString(),
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

        binding.btnLocal.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (isLocalSelected) selectedColor else unselectedColor
        )
        binding.btnRemote.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (isLocalSelected) unselectedColor else selectedColor
        )
    }


    private fun setupRecyclerView() {
        songAdapter = SongAdapter(songList, object : OnSongClickListener {
            override fun onSongClick(song: Song) {
                openPlayerFragment(song)
            }
        }, isGridLayout = false)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = songAdapter
    }
    private fun openPlayerFragment(song: Song) {
        val playerFragment = PlayerFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("SongList", ArrayList(songList))
                putParcelable("Selected Song", song)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragContainer, playerFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadSongs(context: Context) {
        songList.clear()
        val songs = SongUtils.getSongsFromDevice(context)

        Log.d("LibraryFragment", "Số bài hát tìm thấy: ${songs.size}")

        songList.addAll(songs)

        if (::songAdapter.isInitialized) {
            songAdapter.notifyDataSetChanged()
        } else {
            setupRecyclerView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
