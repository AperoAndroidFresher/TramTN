package com.example.musicapp.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.musicapp.R
import com.example.musicapp.base.BaseActivity
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.ui.home.HomeFragment
import com.example.musicapp.ui.library.LibraryFragment
import com.example.musicapp.ui.login.LoginActivity
import com.example.musicapp.ui.music.MusicService
import com.example.musicapp.ui.music.PlayerFragment
import com.example.musicapp.ui.playlist.PlaylistFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity(), OnSongClickListener {

    companion object {
        const val REQUEST_CODE_STORAGE_PERMISSION = 1001
    }

    private lateinit var binding: ActivityMainBinding

    var musicService: MusicService? = null
    private var isBound = false

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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(exitReceiver, IntentFilter("com.example.musicapp.ACTION_EXIT"), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(exitReceiver, IntentFilter("com.example.musicapp.ACTION_EXIT"))
        }

        binding.miniPlayerInclude.ivMiniClose.setOnClickListener {
            musicService?.stopService()
            binding.miniPlayerInclude.miniPlayerLayout.visibility = View.GONE
        }

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        // Kiểm tra user session
        if (username != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragContainer, HomeFragment())
                .commit()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        checkAndRequestPermissions()

        // Thiết lập BottomNavigationView
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
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragContainer)
                if (currentFragment !is PlayerFragment) {
                    binding.miniPlayerInclude.miniPlayerLayout.visibility = android.view.View.VISIBLE
                }
            }
        })
        val intent = Intent(this, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        setupMiniPlayerEvents()
    }
    fun hideMiniPlayer() {
        Log.d("MainActivity", "Ẩn MiniPlayer sau khi dừng Service")
        binding.miniPlayerInclude.miniPlayerLayout.visibility = View.GONE
    }
    private val exitReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finishAffinity()
        }
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

    private fun setupMiniPlayerEvents() {
        binding.miniPlayerInclude.miniPlayerLayout.setOnClickListener {
            musicService?.currentSong?.value?.let { currentSong ->
                val currentSongList: ArrayList<Song> = musicService?.getCurrentPlaylist() ?: arrayListOf()
                val playerFragment = PlayerFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList("SongList", currentSongList)
                        putParcelable("SelectedSong", currentSong)
                    }
                }
                binding.miniPlayerInclude.miniPlayerLayout.visibility = View.GONE
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragContainer, playerFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // Nút “x” -> dừng service
        binding.miniPlayerInclude.ivMiniClose.setOnClickListener {
            musicService?.stopService()
        }
        // Nút Play/Pause
        binding.miniPlayerInclude.ivMiniPlayPause.setOnClickListener {
            if (musicService?.isPlaying?.value == true) {
                musicService?.pauseSong()
            } else {
                musicService?.resumeSong()
            }
        }
    }
    private fun showMiniPlayer(song: Song) {
        Log.d("MainActivity", "Hiển thị MiniPlayer với bài hát: ${song.title}")

        // Hiển thị MiniPlayer
        binding.miniPlayerInclude.miniPlayerLayout.visibility = View.VISIBLE

        // Cập nhật tiêu đề bài hát và nghệ sĩ
        binding.miniPlayerInclude.tvMiniSongTitle.text = song.title
        binding.miniPlayerInclude.tvMiniArtist.text = song.artist

        // Cập nhật nút Play/Pause theo trạng thái hiện tại của bài hát
        val isPlaying = musicService?.isPlaying?.value ?: false
        val icon = if (isPlaying) R.drawable.ic_stop_player else R.drawable.ic_play
        binding.miniPlayerInclude.ivMiniPlayPause.setImageResource(icon)
    }
    fun showMiniPlayerAfterBack() {
        if (musicService?.currentSong?.value != null) {
            binding.miniPlayerInclude.miniPlayerLayout.visibility = View.VISIBLE
            binding.miniPlayerInclude.tvMiniSongTitle.text = musicService?.currentSong?.value?.title
            binding.miniPlayerInclude.tvMiniArtist.text = musicService?.currentSong?.value?.artist
        }
    }
    fun updateMiniPlayer() {
        val song = musicService?.currentSong?.value
        if (song != null) {
            binding.miniPlayerInclude.miniPlayerLayout.visibility = View.VISIBLE
            binding.miniPlayerInclude.tvMiniSongTitle.text = song.title
            binding.miniPlayerInclude.tvMiniArtist.text = song.artist

            val isPlaying = musicService?.isPlaying?.value ?: false
            val icon = if (isPlaying) R.drawable.ic_play else R.drawable.ic_stop_player
            binding.miniPlayerInclude.ivMiniPlayPause.setImageResource(icon)
        } else {
            binding.miniPlayerInclude.miniPlayerLayout.visibility = View.GONE
        }
    }


    private fun observeServiceState() {
        musicService?.currentSong?.observe(this) { song ->
            updateMiniPlayer()
        }

        musicService?.isPlaying?.observe(this) { playing ->
            updateMiniPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        unregisterReceiver(exitReceiver)
    }

    override fun onSongClick(song: Song) {
        Log.d("MainActivity", "Bài hát được chọn: ${song.title}")
        showMiniPlayer(song)
    }


}
