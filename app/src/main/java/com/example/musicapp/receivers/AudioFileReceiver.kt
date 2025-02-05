package com.example.musicapp.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class AudioFileReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == Intent.ACTION_MEDIA_SCANNER_FINISHED) {
//            val songs = getSongsFromDevice(context)
//            songs.forEach { song ->
//                Log.d("AudioFileReceiver", "Found Song: ${song.title} by ${song.artist}")
//            }
//        }
    }
}
