package com.example.musicapp.data.local.dao

import androidx.room.*
import com.example.musicapp.data.local.entity.PlaylistSong
import com.example.musicapp.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Int, songId: String)

    @Query("""
        SELECT s.* FROM songs s 
        INNER JOIN playlist_songs ps 
        ON s.songId = ps.songId 
        WHERE ps.playlistId = :playlistId
    """)
    fun getSongsInPlaylist(playlistId: Int): Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongCount(playlistId: Int): Flow<Int>

    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlist_songs ON songs.songId = playlist_songs.songId
        WHERE playlist_songs.playlistId = :playlistId
        ORDER BY playlist_songs.orderIndexLinear ASC
    """)
    fun getSongsByPlaylistLinear(playlistId: Int): kotlinx.coroutines.flow.Flow<List<Song>>

    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlist_songs ON songs.songId = playlist_songs.songId
        WHERE playlist_songs.playlistId = :playlistId
        ORDER BY playlist_songs.orderIndexGrid ASC
    """)
    fun getSongsByPlaylistGrid(playlistId: Int): Flow<List<Song>>

    @Update
    suspend fun updatePlaylistSongs(songs: List<PlaylistSong>)

}

