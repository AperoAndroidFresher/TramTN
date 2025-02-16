package com.example.musicapp.ui.song

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.Song
import com.example.musicapp.data.local.repository.SongRepository
import com.example.musicapp.utils.SongUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongViewModel(private val repository: SongRepository) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun syncMusic(context: Context) {
        viewModelScope.launch {
            val deviceSongs = SongUtils.getSongsFromDevice(context)
            val dbSongs = allSongs.value

            val newSongs = deviceSongs.filter { deviceSong ->
                dbSongs.none { dbSong -> dbSong.songId == deviceSong.songId }
            }

            if (newSongs.isNotEmpty()) {
                repository.insertSongs(newSongs)
            }
        }
    }

}
