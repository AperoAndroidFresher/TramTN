package com.example.musicapp.base.listeners

import com.example.musicapp.models.Song

interface OnSongClickListener {
    fun onSongClick(song: Song)
}