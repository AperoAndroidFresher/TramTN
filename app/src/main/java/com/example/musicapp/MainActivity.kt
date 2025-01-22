package com.example.musicapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val songs = getSongsFromDevice(this)

        if (songs.isNotEmpty()) {
            val song = songs[0]
            playAudio(song)
        }

        checkAndRequestPermissions()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val selectedFragment = when (menuItem.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_library -> LibraryFragment()
                R.id.nav_playlist -> PlaylistFragment()
                else -> null
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .addToBackStack(null)
                    .commit()
                true
            } else {
                false
            }
        }
    }
    fun playAudio(song: Song) {
        val mediaPlayer = MediaPlayer()

        try {
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
                accessExternalStorage()
            } else {
                Log.e("Permission", "Permissions denied")
                Toast.makeText(this, "Bạn đã từ chối cấp quyền", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun accessExternalStorage() {
        Toast.makeText(this, "Truy cập bộ nhớ ngoài thành công!", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
