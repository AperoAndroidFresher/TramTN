package com.example.musicapp.fragments

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.FragmentLibraryBinding
import com.example.musicapp.models.Song
import com.example.musicapp.utils.SongUtils
import android.util.Log // ✅ Thêm log để debug
import android.widget.Toast
import com.example.musicapp.base.listeners.OnSongClickListener

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

    }

    private fun updateTabUI() {
        if (isLocalSelected) {
            binding.btnLocal.setBackgroundColor(resources.getColor(R.color.teal_700))
            binding.btnRemote.setBackgroundColor(resources.getColor(R.color.gray))
        } else {
            binding.btnLocal.setBackgroundColor(resources.getColor(R.color.gray))
            binding.btnRemote.setBackgroundColor(resources.getColor(R.color.teal_700))
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(songList, object : OnSongClickListener {
            override fun onSongClick(song: Song) {
                playAudio(song)
                Toast.makeText(requireContext(), "Playing: ${song.title}", Toast.LENGTH_SHORT).show()
            }
        }, isGridLayout = false)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = songAdapter
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

    private fun playAudio(song: Song) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(requireContext(), Uri.parse(song.songUri))
                prepare()
                start()
            }
            Log.d("MediaPlayer", "Đang chơi: ${song.title} by ${song.artist}")
            Toast.makeText(requireContext(), "Đang chơi: ${song.title} by ${song.artist}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Lỗi phát nhạc: ${song.title}", e)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
