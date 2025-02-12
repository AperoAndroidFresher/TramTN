package com.example.musicapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.musicapp.models.Song

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
                        val id = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown Title"
                        val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
                        val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        val duration = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                        val songUri = Uri.withAppendedPath(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        ).toString()

                        val song = Song(id, title, artist, songUri, "content://media/external/audio/albumart/$albumId", duration)
                        songList.add(song)

                    } while (it.moveToNext())
                }
            }
        } catch (e: Exception) {
            Log.e("SongList", "Error retrieving songs: ${e.message}", e)
        }

        return songList
    }

    private fun getAlbumArt(filePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val art = retriever.embeddedPicture
            retriever.release()
            if (art != null) {
                BitmapFactory.decodeByteArray(art, 0, art.size)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
