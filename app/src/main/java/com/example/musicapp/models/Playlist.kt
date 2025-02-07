package com.example.musicapp.models

data class Playlist(
    val id: Int,
    var title: String,
    val songs: MutableList<Song>
)

