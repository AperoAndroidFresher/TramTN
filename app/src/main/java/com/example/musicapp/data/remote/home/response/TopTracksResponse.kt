package com.example.musicapp.data.remote.home.response

import com.google.gson.annotations.SerializedName

data class TopTracksResponse(
    @SerializedName("toptracks") val topTracks: TracksList
)

data class TracksList(
    @SerializedName("track") val track: List<Track>
)

data class Track(
    @SerializedName("name") val name: String,
    @SerializedName("playcount") val playcount: String,
    @SerializedName("listeners") val listeners: String,
    @SerializedName("artist") val artist: Artist,
    @SerializedName("image") val image: List<Image>
)
