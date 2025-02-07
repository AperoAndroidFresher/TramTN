package com.example.musicapp.fragments

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.FragmentSongListBinding
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.models.Song
import com.example.musicapp.utils.SongUtils.getSongsFromDevice
import androidx.fragment.app.setFragmentResultListener

class SongListFragment : Fragment(), OnSongClickListener {

    private var mediaPlayer: MediaPlayer? = null
    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!
    private lateinit var songAdapter: SongAdapter
    private var isGridLayout = false
    private lateinit var songList: MutableList<Song>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaPlayer = MediaPlayer()
        songList = getSongsFromDevice(requireContext()).toMutableList()

        if (songList.isNotEmpty()) {
            songAdapter = SongAdapter(songList, this, isGridLayout)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = songAdapter
        } else {
            Log.e("SongListFragment", "Không tìm thấy bài hát nào trên thiết bị.")
        }

        binding.toggleLayoutButton.setOnClickListener {
            toggleLayout()
        }

        binding.btnSort.setOnClickListener {
            val sortingFragment = SortingFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragContainer, sortingFragment)
                .addToBackStack(null)
                .commit()
        }
        
        parentFragmentManager.setFragmentResultListener("sorting_result", this) { _, bundle ->
            val sortedSongs = bundle.getParcelableArrayList<Song>("sortedSongs")
            sortedSongs?.let {
                updatePlaylist(it)
            }
        }
    }

    override fun onSongClick(song: Song) {
        playAudio(song)
    }

    private fun playAudio(song: Song) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.apply {
                setDataSource(requireContext(), Uri.parse(song.songUri))
                prepare()
                start()
                Log.d("MediaPlayer", "Đang chơi: ${song.title} by ${song.artist}")
                Toast.makeText(requireContext(), "Đang chơi: ${song.title} by ${song.artist}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Lỗi phát nhạc: ${song.title}", e)
        }
    }

    private fun updatePlaylist(sortedSongs: List<Song>) {
        songList.clear()
        songList.addAll(sortedSongs)
        songAdapter.notifyDataSetChanged()
    }

    private fun toggleLayout() {
        isGridLayout = !isGridLayout
        binding.recyclerView.layoutManager = if (isGridLayout) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
        songAdapter.setLayoutType(isGridLayout)
        binding.toggleLayoutButton.setImageResource(
            if (isGridLayout) R.drawable.ic_grid_form else R.drawable.ic_linear_form
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}
