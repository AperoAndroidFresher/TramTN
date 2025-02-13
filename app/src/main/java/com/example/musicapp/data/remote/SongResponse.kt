package com.example.musicapp.data.remote

data class SongResponse (
    val title: String,
    val artist: String,
    val kind: String,
    val duration: Long,
    val path: String
)