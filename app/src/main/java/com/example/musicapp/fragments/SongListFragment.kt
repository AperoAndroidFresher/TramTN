package com.example.musicapp.fragments

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.interfaces.OnSongClickListener
import com.example.musicapp.models.Song
import com.example.musicapp.utils.SongUtils.getSongsFromDevice

class SongListFragment : Fragment(), OnSongClickListener {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var songDurationTextView: TextView
    private lateinit var toggleLayoutButton: ImageButton
    private lateinit var songAdapter: SongAdapter // Khai báo songAdapter
    private var isGridLayout = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaPlayer = MediaPlayer()
        recyclerView = view.findViewById(R.id.recyclerView)
        songDurationTextView = view.findViewById(R.id.songDuration)
        toggleLayoutButton = view.findViewById(R.id.toggleLayoutButton)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val songs = getSongsFromDevice(requireContext())
        if (songs.isNotEmpty()) {
            val adapter = SongAdapter(songs, this)
            recyclerView.adapter = adapter
            songAdapter = adapter
        } else {
            Log.e("SongListFragment", "Không tìm thấy bài hát nào trên thiết bị.")
        }

        toggleLayoutButton.setOnClickListener {
            toggleLayout()
        }
    }

    override fun onSongClick(song: Song) {
        playAudio(song)
    }

    private fun playAudio(song: Song) {
        try {
            mediaPlayer?.reset()

            mediaPlayer?.apply {
                setDataSource(requireContext(), Uri.parse(song.uri))
                prepare()
                start()

                Log.d("MediaPlayer", "Đang chơi: ${song.title} by ${song.artist}")
                Toast.makeText(requireContext(), "Đang chơi: ${song.title} by ${song.artist}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Lỗi phát nhạc: ${song.title}", e)
        }
    }

    private fun toggleLayout() {
        isGridLayout = !isGridLayout

        songAdapter.setLayoutType(isGridLayout)

        if (isGridLayout) {
            toggleLayoutButton.setImageResource(R.drawable.ic_linear_form)
        } else {
            toggleLayoutButton.setImageResource(R.drawable.ic_grid_form)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

