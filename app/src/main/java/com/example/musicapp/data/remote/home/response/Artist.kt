package com.example.musicapp.data.remote.home.response

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: List<Image>
)