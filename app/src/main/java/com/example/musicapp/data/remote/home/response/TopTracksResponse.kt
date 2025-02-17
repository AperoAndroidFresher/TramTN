package com.example.musicapp.data.remote.home.response

import com.google.gson.annotations.SerializedName

data class TopTracksResponse(
    @SerializedName("tracks") val tracks: TrackList
)

data class TrackList(
    @SerializedName("track") val track: List<Track>
)