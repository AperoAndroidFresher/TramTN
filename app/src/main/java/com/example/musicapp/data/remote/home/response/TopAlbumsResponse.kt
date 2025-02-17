package com.example.musicapp.data.remote.home.response

import com.google.gson.annotations.SerializedName

data class TopAlbumsResponse(
    @SerializedName("albums") val albums: AlbumList
)

data class AlbumList(
    @SerializedName("album") val album: List<Album>
)