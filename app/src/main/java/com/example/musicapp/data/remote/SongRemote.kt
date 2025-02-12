package com.example.musicapp.data.remote

data class SongRemote (
    val title: String,
    val artist: String,
    val kind: String,
    val duration: Long,
    val path: String
)