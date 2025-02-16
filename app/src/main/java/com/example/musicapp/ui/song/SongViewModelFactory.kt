package com.example.musicapp.ui.song

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.data.local.repository.SongRepository

class SongViewModelFactory(private val repository: SongRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
            return SongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}