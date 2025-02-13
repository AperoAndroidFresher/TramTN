package com.example.musicapp.ui.player

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicapp.R
import com.example.musicapp.databinding.FragmentPlayerBinding
import com.example.musicapp.models.Song

class PlayerFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var binding: FragmentPlayerBinding
    private var currentSong: Song? = null
    private val handler = Handler(Looper.getMainLooper())

    private var songList: List<Song> = listOf()
    private var currentSongIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songList = arguments?.getParcelableArrayList("SongList") ?: listOf()

        currentSong = arguments?.getParcelable("Selected Song")

        currentSong?.let { song ->
            currentSongIndex = songList.indexOfFirst { it.songUri == song.songUri }
            updateUI(song)
            playAudio(song)
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        binding.btnNext.setOnClickListener {
            playNextSong()
        }

        binding.btnPrev.setOnClickListener {
            playPreviousSong()
        }

        setupSeekBar()
    }
    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    binding.tvCurrentTime.text = formatDuration(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun updateUI(song: Song) {
        binding.tvSongTitle.text = song.title
        binding.tvArtist.text = song.artist
        if (song.albumArt.isNotEmpty()) {
            binding.imgAlbumArt.setImageURI(Uri.parse(song.albumArt))
        } else {
            binding.imgAlbumArt.setImageResource(R.drawable.img_default_music_art)
        }
    }

    private fun playAudio(song: Song) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(requireContext(), Uri.parse(song.songUri))
            prepare()
            start()

            binding.seekBar.max = duration
            binding.tvTotalTime.text = formatDuration(duration.toLong())

            handler.post(updateSeekBar)
        }
    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val currentPosition = it.currentPosition
                binding.seekBar.progress = currentPosition
                binding.tvCurrentTime.text = formatDuration(currentPosition.toLong())
                handler.postDelayed(this, 500)
            }
        }
    }

    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                binding.btnPlayPause.setImageResource(R.drawable.ic_stop_player)
            } else {
                it.start()
                binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun playNextSong() {
        if (songList.isEmpty()) {
            Log.e("PlayerFragment", "Song list is empty!")
            return
        }

        if (currentSongIndex < songList.size - 1) {
            currentSongIndex++
            Log.d("PlayerFragment", "Next song index: $currentSongIndex")
            currentSong = songList[currentSongIndex]
            currentSong?.let {
                updateUI(it)
                playAudio(it)
            }
        } else {
            Log.d("PlayerFragment", "Already at the last song!")
        }
    }

    private fun playPreviousSong() {
        if (songList.isEmpty()) {
            Log.e("PlayerFragment", "Song list is empty!")
            return
        }
        if (currentSongIndex > 0) {
            currentSongIndex--
            Log.d("PlayerFragment", "Previous song index: $currentSongIndex")

            currentSong = songList[currentSongIndex]
            currentSong?.let {
                updateUI(it)
                playAudio(it)
            }
        } else {
            Log.d("PlayerFragment", "Already at the first song!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateSeekBar)
    }
}
