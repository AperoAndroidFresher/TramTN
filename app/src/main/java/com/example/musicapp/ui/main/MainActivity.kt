package com.example.musicapp.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.musicapp.R
import com.example.musicapp.ui.playlist.PlaylistDialogFragment
import com.example.musicapp.ui.home.HomeFragment
import com.example.musicapp.ui.library.LibraryFragment
import com.example.musicapp.ui.playlist.PlaylistFragment
import com.example.musicapp.models.Playlist
import com.example.musicapp.models.Song
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
//    private var mediaPlayer: MediaPlayer? = null
    private val playlists = mutableListOf<Playlist>()

    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestPermissions()
//        playlists.add(Playlist(id = 1, title = "Favorites", songs = mutableListOf()))

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_bottom)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragContainer, HomeFragment())
                .commit()
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->

            val fragContainer = findViewById<FrameLayout>(R.id.fragContainer)

            when (menuItem.itemId) {
                R.id.navPlaylist -> {
//                    stopMediaPlayer()
                    replaceFragment(PlaylistFragment(), true)
                    true
                }
                R.id.navLibrary -> {
                    fragContainer.visibility = FrameLayout.VISIBLE
                    replaceFragment(LibraryFragment(), false)
                    true
                }
                R.id.navHome -> {
                    fragContainer.visibility = FrameLayout.VISIBLE
                    replaceFragment(HomeFragment(), false)
                    true
                }
                else -> false
            }
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        })
    }
    fun getPlaylists(): List<Playlist> {
        return playlists
    }
    fun addPlaylist(name: String, song: Song) {
        val newId = (playlists.maxOfOrNull { it.id } ?: 0) + 1
        val newPlaylist = Playlist(newId, name, mutableListOf())
        playlists.add(newPlaylist)

        val fragment = supportFragmentManager.findFragmentByTag("PlaylistFragment") as? PlaylistFragment
        fragment?.updatePlaylists(playlists)

        val playlistDialog = PlaylistDialogFragment.newInstance(song)
        playlistDialog.show(supportFragmentManager, "PlaylistDialogFragment")
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean) {
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            val transaction = supportFragmentManager.beginTransaction()
                .replace(R.id.fragContainer, fragment)

            if (addToBackStack) {
                transaction.addToBackStack(null)
            }

            transaction.commit()
        }
    }


//    private fun stopMediaPlayer() {
//        mediaPlayer?.apply {
//            if (isPlaying) {
//                stop()
//                release()
//            }
//        }
//        mediaPlayer = null
//    }


    private fun checkAndRequestPermissions() {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO
            )
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
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
            } else {
                Log.e("Permission", "Permissions denied")
                Toast.makeText(this, "Bạn đã từ chối cấp quyền", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
