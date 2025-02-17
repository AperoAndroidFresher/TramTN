package com.example.musicapp.data.remote.home.response

import com.google.gson.annotations.SerializedName

data class TopArtistsResponse(
    @SerializedName("artists") val artists: ArtistList
)

data class ArtistList(
    @SerializedName("artist") val artist: List<Artist>
)