package com.example.musicapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicapp.data.local.dao.PlaylistDao
import com.example.musicapp.data.local.dao.PlaylistSongDao
import com.example.musicapp.data.local.dao.SongDao
import com.example.musicapp.data.local.dao.UserDao
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.PlaylistSong
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.data.local.entity.User

@Database(entities = [User::class,
    Playlist::class,
    Song::class,
    PlaylistSong::class],
    version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao
    abstract fun playlistSongDao(): PlaylistSongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_app_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
