package com.example.musicapp.data.remote.home.response

import com.google.gson.annotations.SerializedName

data class TopAlbumsResponse(
    @SerializedName("topalbums") val topAlbums: AlbumsList
)

data class AlbumsList(
    @SerializedName("album") val album: List<Album>
)

data class Album(
    @SerializedName("name") val name: String,
    @SerializedName("artist") val artist: Artist,
    @SerializedName("image") val image: List<Image>
)

