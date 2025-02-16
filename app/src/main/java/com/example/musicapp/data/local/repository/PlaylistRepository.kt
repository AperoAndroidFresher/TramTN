package com.example.musicapp.data.local.repository

import com.example.musicapp.data.local.dao.PlaylistDao
import com.example.musicapp.data.local.dao.PlaylistSongDao
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.PlaylistSong
import com.example.musicapp.data.local.entity.Song
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao
) {

    suspend fun addSongToPlaylist(playlistId: Int, songId: String) {
        playlistSongDao.insertPlaylistSong(PlaylistSong(playlistId, songId))
    }

    suspend fun updatePlaylistTitle(playlistId: Int, newTitle: String) {
        playlistDao.updatePlaylistTitle(playlistId, newTitle)
    }

    fun getSongCount(playlistId: Int): Flow<Int> {
        return playlistSongDao.getSongCount(playlistId)
    }


    suspend fun getPlaylistsForUser(userId: Int): List<Playlist> {
        return playlistDao.getPlaylistsForUser(userId)
    }

    suspend fun addPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(playlist)
    }

    suspend fun renamePlaylist(playlistId: Int, newTitle: String) {
        playlistDao.updatePlaylistTitle(playlistId, newTitle)
    }

    suspend fun deletePlaylistById(playlistId: Int) {
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun updatePlaylistSongOrder(playlistSongs: List<PlaylistSong>) {
        playlistSongDao.updatePlaylistSongs(playlistSongs)
    }

    fun getSongsByPlaylistSorted(playlistId: Int, isGridLayout: Boolean): Flow<List<Song>> {
        return if (isGridLayout) {
            playlistSongDao.getSongsByPlaylistGrid(playlistId)
        } else {
            playlistSongDao.getSongsByPlaylistLinear(playlistId)
        }
    }

}


