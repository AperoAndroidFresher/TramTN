package com.example.musicapp.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val songUri: String,
    val albumArt: String,
    val duration: Long,
    ) : Parcelable
