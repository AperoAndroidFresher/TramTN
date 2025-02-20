package com.example.musicapp.ui.music

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicapp.R
import com.example.musicapp.data.local.entity.Song

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "MUSIC_CHANNEL_ID"
        const val NOTIFICATION_ID = 1

        const val ACTION_PLAY_PAUSE = "com.example.musicapp.ACTION_PLAY_PAUSE"
        const val ACTION_CLOSE      = "com.example.musicapp.ACTION_CLOSE"
        const val ACTION_NEXT       = "com.example.musicapp.ACTION_NEXT"
        const val ACTION_PRE        = "com.example.musicapp.ACTION_PRE"
        const val ACTION_SHUFFLE    = "com.example.musicapp.ACTION_SHUFFLE"
        const val ACTION_REPEAT     = "com.example.musicapp.ACTION_REPEAT"
    }

    private val originalPlaylist = mutableListOf<Song>()
    private val shuffledPlaylist = mutableListOf<Song>()
    private var currentPlaylist = originalPlaylist
    private var currentIndex = 0

    private var isShuffleEnabled = false
    private var isRepeatEnabled  = false

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _currentSong = MutableLiveData<Song?>(null)
    val currentSong: LiveData<Song?> get() = _currentSong

    private var mediaPlayer: MediaPlayer? = null

    private val binder = MusicBinder()
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        handleIntentAction(intent)
        val notification = createCustomNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 (API 34)
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("MusicService", "startForeground() failed", e)
            try {
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
            } catch (se: SecurityException) {
                Log.e("MusicService", "SecurityException while notifying", se)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        originalPlaylist.clear()
        originalPlaylist.addAll(songs)
        shuffledPlaylist.clear()
        shuffledPlaylist.addAll(songs.shuffled())
        currentPlaylist = if (isShuffleEnabled) shuffledPlaylist else originalPlaylist
        currentIndex = startIndex.coerceIn(0, currentPlaylist.size - 1)
        playIndex(currentIndex)
    }

    fun playSong(song: Song) {
        Log.d("MusicService", "playSong() được gọi với bài hát: ${song.title}")
        val index = originalPlaylist.indexOf(song)
        if (index != -1) {
            currentIndex = index
        } else {
            originalPlaylist.add(song)
            currentIndex = originalPlaylist.size - 1
        }
        playIndex(currentIndex)
    }

    fun nextSong() {
        if (currentPlaylist.isEmpty()) return
        currentIndex = (currentIndex + 1) % currentPlaylist.size
        Log.d("MusicService", "⏭ Chuyển bài: ${currentPlaylist[currentIndex].title}")
        playIndex(currentIndex)
    }

    fun preSong() {
        if (currentPlaylist.isEmpty()) return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else currentPlaylist.size - 1
        Log.d("MusicService", "⏮ Quay lại bài: ${currentPlaylist[currentIndex].title}")
        playIndex(currentIndex)
    }

    fun pauseSong() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                updateNotification()
            }
        }
    }

    fun resumeSong() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
                updateNotification()
            }
        }
    }

    fun stopSong() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Lỗi khi dừng MediaPlayer", e)
        }
        _isPlaying.value = false
        _currentSong.value = null
        // updateNotification()
    }

    fun stopService() {
        stopSong()
        try {
            stopForeground(true)
        } catch (e: Exception) {
            Log.e("MusicService", "Lỗi khi dừng foreground", e)
        }
        getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
        stopSelf()
    }


    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        val current = _currentSong.value ?: return
        currentPlaylist = if (isShuffleEnabled) {
            shuffledPlaylist.apply { shuffle() }
        } else {
            originalPlaylist
        }
        currentIndex = currentPlaylist.indexOfFirst { it.songId == current.songId }
    }

    fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
        Log.d("MusicService", "Chế độ Repeat: $isRepeatEnabled")
        updateNotification()
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.let {
            if (it.isPlaying || _isPlaying.value == true) it.currentPosition else 0
        } ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.let {
            if (it.isPlaying || _isPlaying.value == true) it.duration else 0
        } ?: 0
    }

    fun seekTo(pos: Int) {
        mediaPlayer?.let {
            if (it.isPlaying || _isPlaying.value == true) {
                it.seekTo(pos)
            }
        }
    }

    private fun playIndex(index: Int) {
        if (index !in currentPlaylist.indices) return
        val song = currentPlaylist[index]
        _currentSong.postValue(song)
        Log.d("MusicService", "🎵 Đang phát bài mới: ${song.title}")
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            } else {
                mediaPlayer?.reset()
            }
            mediaPlayer?.apply {
                setDataSource(applicationContext, Uri.parse(song.songUri))
                prepare()
                start()
                setOnCompletionListener {
                    when {
                        isRepeatEnabled -> {
                            Log.d("MusicService", "⏭ Lặp lại bài: ${song.title}")
                            playIndex(currentIndex)
                        }
                        isShuffleEnabled -> {
                            if (currentPlaylist.size > 1) {
                                var randomIndex: Int
                                do {
                                    randomIndex = (0 until currentPlaylist.size).random()
                                } while (randomIndex == currentIndex)
                                currentIndex = randomIndex
                            }
                            Log.d("MusicService", "⏭ Chọn ngẫu nhiên bài: ${currentPlaylist[currentIndex].title}")
                            playIndex(currentIndex)
                        }
                        else -> {
                            nextSong()
                        }
                    }
                }
            }
            _isPlaying.postValue(true)
            updateNotification()
        } catch (e: Exception) {
            Log.e("MusicService", "Lỗi khi phát nhạc", e)
        }
    }

    private fun handleIntentAction(intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            ACTION_PLAY_PAUSE -> {
                if (_isPlaying.value == true) pauseSong() else resumeSong()
            }
            ACTION_CLOSE -> {
                stopService()
            }
            ACTION_NEXT -> nextSong()
            ACTION_PRE -> preSong()
            ACTION_SHUFFLE -> {
                toggleShuffle()
                updateNotification()
            }
            ACTION_REPEAT -> {
                toggleRepeat()
                updateNotification()
            }
        }
    }
    private fun createCustomNotification(): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.notification_music)

        remoteViews.setTextViewText(R.id.tvBrand, "Apero Music")
        val trackNumber = if (currentPlaylist.isNotEmpty()) {
            "${currentIndex + 1}/${currentPlaylist.size}"
        } else {
            "1/10"
        }
        remoteViews.setTextViewText(R.id.tvTrackNumber, trackNumber)

        val songTitle = _currentSong.value?.title ?: "No song"
        remoteViews.setTextViewText(R.id.tvSongTitle, songTitle)
        val artist = _currentSong.value?.artist ?: ""
        remoteViews.setTextViewText(R.id.tvArtist, artist)
        val playPauseIcon = if (_isPlaying.value == true) R.drawable.ic_play else R.drawable.ic_stop_player
        remoteViews.setImageViewResource(R.id.ivPlayPause, playPauseIcon)


        remoteViews.setOnClickPendingIntent(R.id.ivPre, pendingIntentForAction(ACTION_PRE))
        remoteViews.setOnClickPendingIntent(R.id.ivPlayPause, pendingIntentForAction(ACTION_PLAY_PAUSE))
        remoteViews.setOnClickPendingIntent(R.id.ivNext, pendingIntentForAction(ACTION_NEXT))
        remoteViews.setOnClickPendingIntent(R.id.ivClose, pendingIntentForAction(ACTION_CLOSE))

        val clickIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .build()
    }

//    private fun createSimpleNotification(): Notification {
//        val clickIntent = packageManager?.getLaunchIntentForPackage(packageName)
//        val contentPendingIntent = PendingIntent.getActivity(
//            this, 0, clickIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        val songTitle = _currentSong.value?.title ?: "No song"
//        val playPauseIcon = if (_isPlaying.value == true) R.drawable.ic_stop_player else R.drawable.ic_play
//
//        return NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_music)
//            .setContentTitle("Apero Music")
//            .setContentText("Đang phát: $songTitle")
//            .setContentIntent(contentPendingIntent)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setOnlyAlertOnce(true)
//            .addAction(playPauseIcon, "Play/Pause", pendingIntentForAction(ACTION_PLAY_PAUSE))
//            .build()
//    }


    private fun updateNotification() {
        val notification = createCustomNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("MusicService", "POST_NOTIFICATIONS permission chưa được cấp")
                return
            }
        }
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        } catch (se: SecurityException) {
            Log.e("MusicService", "SecurityException while notifying", se)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Music playback controls"
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun pendingIntentForAction(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun getCurrentPlaylist(): ArrayList<Song> = ArrayList(currentPlaylist)
}
