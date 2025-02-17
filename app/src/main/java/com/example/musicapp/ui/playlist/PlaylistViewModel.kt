package com.example.musicapp.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.PlaylistSong
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.data.local.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PlaylistViewModel(private val repository: PlaylistRepository) : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    fun loadPlaylists(userId: Int) {
        viewModelScope.launch {
            val playlists = repository.getPlaylistsForUser(userId)
            _playlists.postValue(playlists)
        }
    }

    fun updatePlaylistTitle(playlistId: Int, newTitle: String, userId: Int) {
        viewModelScope.launch {
            repository.updatePlaylistTitle(playlistId, newTitle)
            loadPlaylists(userId)
        }
    }


    fun addPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.addPlaylist(playlist)
            loadPlaylists(playlist.userId)
        }
    }
    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.renamePlaylist(playlist.playlistId, playlist.title)
            loadPlaylists(playlist.userId)
        }
    }
    fun deletePlaylist(playlistId: Int, userId: Int) {
        viewModelScope.launch {
            repository.deletePlaylistById(playlistId)
            loadPlaylists(userId)
        }
    }

    //playlistSong

    fun addSongToPlaylist(playlistId: Int, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }
    fun getSongCountLiveData(playlistId: Int): LiveData<Int> {
        return repository.getSongCount(playlistId).asLiveData()
    }
    fun getSongsByPlaylist(playlistId: Int, isGridLayout: Boolean): Flow<List<Song>> {
        return repository.getSongsByPlaylistSorted(playlistId, isGridLayout)
    }

    fun updatePlaylistSongsOrder(songs: List<PlaylistSong>) {
        viewModelScope.launch {
            repository.updatePlaylistSongOrder(songs)
        }
    }
    fun removeSongFromPlaylist(playlistId: Int, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

}
