package com.example.musicapp.models

import android.content.Context
import android.provider.MediaStore
import android.util.Log

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val uri: String,
    val albumArt: String?
)
fun getSongsFromDevice(context: Context): List<Song> {
    val songList = mutableListOf<Song>()

    val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID
    )

    val selection = "${MediaStore.Audio.Media.MIME_TYPE} IN (?, ?)"
    val selectionArgs = arrayOf("audio/mpeg", "audio/mp4")

    val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

    try {
        val cursor = context.contentResolver.query(
            audioUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown Title"
                    val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                    val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                    // Lấy ảnh album
                    val albumArt = getAlbumArt(context, albumId)

                    val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.buildUpon()
                        .appendPath(id)
                        .build()
                        .toString()

                    val song = Song(id, title, artist, songUri, albumArt)
                    songList.add(song)

                    Log.d("SongList", "Song found: $title by $artist, AlbumArt: $albumArt")
                } while (it.moveToNext())
            }
        }
    } catch (e: Exception) {
        Log.e("SongList", "Error retrieving songs: ${e.message}", e)
    }

    return songList
}

fun getAlbumArt(context: Context, albumId: Long): String? {
    val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)

    val selection = "${MediaStore.Audio.Albums._ID} = ?"
    val selectionArgs = arrayOf(albumId.toString())

    val cursor = context.contentResolver.query(
        albumUri,
        projection,
        selection,
        selectionArgs,
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
        }
    }

    return null
}

