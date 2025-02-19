package com.example.musicapp.data.remote.home.response

import com.google.gson.annotations.SerializedName

data class TopArtistsResponse(
    @SerializedName("artists") val topArtists: ArtistsLists
)

data class ArtistsLists(
    @SerializedName("artist") val artist: List<Artist>,

)
