package com.example.musicapp.data.local.dao

import androidx.room.*
import com.example.musicapp.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    @Query("DELETE FROM songs")
    suspend fun clearAllSongs()
    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlist_songs ON songs.songId = playlist_songs.songId
        WHERE playlist_songs.playlistId = :playlistId
    """)
    fun getSongsByPlaylistId(playlistId: Int): Flow<List<Song>>
    @Update
    suspend fun updateSongs(songs: List<Song>)

}
