package com.example.musicapp.models

import android.content.Context
import android.provider.MediaStore
import android.util.Log

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val uri: String
)
fun getSongsFromDevice(context: Context): List<Song> {
    val songList = mutableListOf<Song>()
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID
    )

    val selection = "${MediaStore.Audio.Media.MIME_TYPE} = ?"
    val selectionArgs = arrayOf("audio/mpeg")

    val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)

    cursor?.use {
        if (it.moveToFirst()) {
            do {
                val id = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val albumId = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendPath(id)
                    .build()
                    .toString()

                val song = Song(id, title, artist, songUri)
                Log.d("SongList", "Song found: $title by $artist, URI: $songUri")
                songList.add(song)
            } while (it.moveToNext())
        }
        it.close()
    }
    if (songList.isEmpty()) {
        Log.e("SongList", "No songs found in device.")
    } else {

        Log.d("SongList", "Found ${songList.size} songs.")
        songList.forEach { song ->
            Log.d("SongList", "Song: ${song.title} by ${song.artist}, URI: ${song.uri}")
        }
    }

    return songList
}

