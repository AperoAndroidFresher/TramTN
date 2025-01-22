package com.example.musicapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

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
