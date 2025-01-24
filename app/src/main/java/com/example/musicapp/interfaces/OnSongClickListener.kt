package com.example.musicapp.interfaces

import com.example.musicapp.models.Song

interface OnSongClickListener {
    fun onSongClick(song: Song)
}