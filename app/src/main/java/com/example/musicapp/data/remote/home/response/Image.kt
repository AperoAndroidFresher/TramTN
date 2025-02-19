package com.example.musicapp.data.remote.home.response

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("#text") val url: String,
    @SerializedName("size") val size: String
)