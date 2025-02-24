package com.example.musicapp.data.local.repository

import com.example.musicapp.data.local.dao.SongDao
import com.example.musicapp.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

class SongRepository(private val songDao: SongDao) {

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()

    suspend fun insertSongs(songs: List<Song>) {
        songDao.insertSongs(songs)
    }

    companion object {
        @Volatile
        private var INSTANCE: SongRepository? = null

        fun getInstance(songDao: SongDao): SongRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SongRepository(songDao).also { INSTANCE = it }
            }
        }
    }
}
