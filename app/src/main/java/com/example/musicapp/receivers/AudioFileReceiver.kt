package com.example.musicapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.musicapp.utils.SongUtils.getSongsFromDevice


class AudioFileReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MEDIA_SCANNER_FINISHED) {
            val songs = getSongsFromDevice(context)
            songs.forEach { song ->
                Log.d("AudioFileReceiver", "Found Song: ${song.title} by ${song.artist}")
            }
        }
    }
}
