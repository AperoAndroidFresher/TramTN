package com.example.musicapp.data.local.dao

import androidx.room.*
import com.example.musicapp.data.local.entity.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlists WHERE userId = :userId")
    suspend fun getPlaylistsForUser(userId: Int): List<Playlist>

    @Query("UPDATE playlists SET title = :newTitle WHERE playlistId = :playlistId")
    suspend fun updatePlaylistTitle(playlistId: Int, newTitle: String)

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylistById(playlistId: Int)

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongCount(playlistId: Long): Flow<Int>
}

