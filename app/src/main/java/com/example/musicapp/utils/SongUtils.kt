package com.example.musicapp.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.musicapp.data.local.entity.Song

object SongUtils {
    fun getSongsFromDevice(context: Context): List<Song> {
        val songList = mutableListOf<Song>()

        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                audioUri,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val idLong = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown Title"
                        val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                        val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        val duration = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, idLong)


                        val albumArtUri = "content://media/external/audio/albumart/$albumId"


                        val song = Song(
                            songId = idLong.toString(),
                            title = title,
                            artist = artist,
                            songUri = contentUri.toString(),
                            albumArt = albumArtUri,
                            duration = duration
                        )
                        songList.add(song)

                    } while (it.moveToNext())
                }
            }
        } catch (e: Exception) {
            Log.e("SongList", "Error retrieving songs: ${e.message}", e)
        }

        return songList
    }
}
