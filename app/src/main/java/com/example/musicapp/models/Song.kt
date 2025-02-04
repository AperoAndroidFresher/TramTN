package com.example.musicapp.models

import android.content.Context
import android.provider.MediaStore
import android.util.Log

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val uri: String,
    val albumArt: String?
)


