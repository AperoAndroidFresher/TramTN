package com.example.musicapp.ui.music

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicapp.R
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.databinding.FragmentPlayerBinding
import com.example.musicapp.ui.main.MainActivity

class PlayerFragment : Fragment() {

    private lateinit var binding: FragmentPlayerBinding

    private var musicService: MusicService? = null
    private var isBound = false

    private var isShuffleEnabled = false
    private var isRepeatEnabled  = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            observeService()

            val songList = arguments?.getParcelableArrayList<Song>("SongList") ?: arrayListOf()
            val selectedSong = arguments?.getParcelable<Song>("SelectedSong")

            if (songList.isNotEmpty() && selectedSong != null) {
                val startIndex = songList.indexOf(selectedSong).coerceAtLeast(0)
                musicService?.setPlaylist(songList, startIndex)
            } else if (selectedSong != null) {
                musicService?.playSong(selectedSong)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
            (activity as? MainActivity)?.showMiniPlayerAfterBack()
        }

        binding.btnClose.setOnClickListener {
            Log.d("PlayerFragment", "Đóng PlayerFragment và dừng Service")

            musicService?.stopService()
            (activity as? MainActivity)?.hideMiniPlayer()
            parentFragmentManager.popBackStack()
        }

        binding.btnPlayPause.setOnClickListener {
            if (musicService?.isPlaying?.value == true) {
                musicService?.pauseSong()
                Log.d("PlayerFragment", "Tạm dừng bài hát")
            } else {
                musicService?.resumeSong()
                Log.d("PlayerFragment", "Tiếp tục phát nhạc")
            }
            (activity as? MainActivity)?.updateMiniPlayer()
        }


        binding.btnNext.setOnClickListener {
            musicService?.nextSong()
            Log.d("PlayerFragment", "Chuyển bài tiếp theo")

            binding.root.postDelayed({
                (activity as? MainActivity)?.updateMiniPlayer()
            }, 1000)
        }

        binding.btnPrev.setOnClickListener {
            musicService?.preSong()
            Log.d("PlayerFragment", "Quay lại bài trước")

            binding.root.postDelayed({
                (activity as? MainActivity)?.updateMiniPlayer()
            }, 1000)
        }


        binding.btnRepeat.setOnClickListener {
            isRepeatEnabled = !isRepeatEnabled
            musicService?.toggleRepeat()

            val color = if (isRepeatEnabled) R.color.white else R.color.login_password_field_white
            binding.btnRepeat.setColorFilter(resources.getColor(color, null))

            Log.d("PlayerFragment", "Chế độ Repeat: $isRepeatEnabled")
        }

        binding.btnShuffle.setOnClickListener {
            isShuffleEnabled = !isShuffleEnabled
            musicService?.toggleShuffle()

            val color = if (isShuffleEnabled) R.color.white else R.color.login_password_field_white
            binding.btnShuffle.setColorFilter(resources.getColor(color, null))

            Log.d("PlayerFragment", "Chế độ Shuffle: $isShuffleEnabled")
        }

        setupSeekBar()
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

    private fun observeService() {
        musicService?.currentSong?.observe(viewLifecycleOwner) { song ->
            if (song != null) {
                updateUI(song)
            }
        }
        musicService?.isPlaying?.observe(viewLifecycleOwner) { playing ->
            val icon = if (playing) R.drawable.ic_play else R.drawable.ic_stop_player
            binding.btnPlayPause.setImageResource(icon)
        }
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        val updateSeekBarRunnable = object : Runnable {
            override fun run() {
                if (isBound) {
                    val pos = musicService?.getCurrentPosition() ?: 0
                    val dur = musicService?.getDuration() ?: 0
                    binding.seekBar.max = dur
                    binding.seekBar.progress = pos

                    binding.tvCurrentTime.text = formatDuration(pos.toLong())
                    binding.tvTotalTime.text = formatDuration(dur.toLong())
                }
                binding.root.postDelayed(this, 500)
            }
        }

        binding.root.removeCallbacks(updateSeekBarRunnable)
        binding.root.post(updateSeekBarRunnable)
    }


    private fun updateUI(song: Song) {
        binding.tvSongTitle.text = song.title
        binding.tvArtist.text = song.artist

        if (!song.albumArt.isNullOrEmpty()) {
            binding.imgAlbumArt.setImageURI(android.net.Uri.parse(song.albumArt))
        } else {
            binding.imgAlbumArt.setImageResource(R.drawable.img_default_music_art)
        }
    }


    private fun formatDuration(duration: Long): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
