package com.example.musicapp.base.listeners

import com.example.musicapp.data.local.entity.Song

interface OnSongClickListener {
    fun onSongClick(song: Song)
}