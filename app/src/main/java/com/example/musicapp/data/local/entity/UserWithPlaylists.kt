package com.example.musicapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithPlaylists(
    @Embedded val user: User,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val playlists: List<Playlist>
)
