package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(entity = Playlist::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"], onDelete = CASCADE),
        ForeignKey(entity = Song::class,
            parentColumns = ["songId"],
            childColumns = ["songId"],
            onDelete = CASCADE)
    ]
)
data class PlaylistSong(
    val playlistId: Int,
    val songId: String,
    var orderIndexLinear: Int = 0,
    var orderIndexGrid: Int = 0
)