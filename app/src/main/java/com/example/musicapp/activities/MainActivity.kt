package com.example.musicapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.models.Song
import com.example.musicapp.fragments.HomeFragment
import com.example.musicapp.fragments.LibraryFragment
import com.example.musicapp.fragments.PlaylistFragment
import com.example.musicapp.interfaces.OnSongClickListener
import com.example.musicapp.models.getSongsFromDevice
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity(), OnSongClickListener {
    private lateinit var mediaPlayer: MediaPlayer


    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer()

        checkAndRequestPermissions()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val songs = getSongsFromDevice(this)
        if (songs.isNotEmpty()) {
            val adapter = SongAdapter(songs,this)
            recyclerView.adapter = adapter
        } else {
            Log.e("MainActivity", "No songs found on device.")
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_bottom)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragContainer, HomeFragment())
                .commit()
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSongs)
            val fragContainer = findViewById<FrameLayout>(R.id.fragContainer)
            when (menuItem.itemId) {
                R.id.navHome -> {

                    recyclerView.visibility = RecyclerView.VISIBLE
                    fragContainer.visibility = FrameLayout.GONE

                    true
                }
                R.id.navLibrary, R.id.navPlaylist -> {

                    recyclerView.visibility = RecyclerView.GONE
                    fragContainer.visibility = FrameLayout.VISIBLE

                    val selectedFragment = when (menuItem.itemId) {
                        R.id.navLibrary -> LibraryFragment()
                        R.id.navPlaylist -> PlaylistFragment()
                        else -> null
                    }
                    if (selectedFragment != null) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragContainer, selectedFragment)
                            .commit()
                    }
                    true
                }
                else -> false
            }
        }
    }
    override fun onSongClick(song: Song) {
        playAudio(song)
    }

    private fun playAudio(song: Song) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, Uri.parse(song.uri))
            mediaPlayer.prepare()
            mediaPlayer.start()

            Log.d("MediaPlayer", "Đang chơi: ${song.title} by ${song.artist}")
            Toast.makeText(this, "Đang chơi: ${song.title} by ${song.artist}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Lỗi phát nhạc: ${song.title}", e)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            else -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                neededPermissions.toTypedArray(),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        } else {
            loadSongsAndPlayFirst()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("Permission", "Permissions granted")
                loadSongsAndPlayFirst()
            } else {
                Log.e("Permission", "Permissions denied")
                Toast.makeText(this, "Bạn đã từ chối cấp quyền", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("Permission", "Unexpected requestCode: $requestCode")
        }
    }


    private fun loadSongsAndPlayFirst() {
        val songs = getSongsFromDevice(this)
        if (songs.isNotEmpty()) {
            val firstSong = songs[0]
            playAudio(firstSong)
        } else {
            Toast.makeText(this, "Không tìm thấy bài hát nào!", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "No songs found on device.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
