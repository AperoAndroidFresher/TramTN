package com.example.musicapp.data.remote.home

import com.example.musicapp.data.remote.home.response.TopAlbumsResponse
import com.example.musicapp.data.remote.home.response.TopArtistsResponse
import com.example.musicapp.data.remote.home.response.TopTracksResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiServiceHome {
    @GET("2.0/?api_key=e65449d181214f936368984d4f4d4ae8&format=json&method=chart.gettopartists")
    suspend fun getTopArtists(): Response<TopArtistsResponse>

    @GET("2.0/?api_key=e65449d181214f936368984d4f4d4ae8&format=json&method=artist.getTopTracks&mbid=f9b593e6-4503-414c-99a0-46595ecd2e23")
    suspend fun getTopTracks(): Response<TopTracksResponse>

    @GET("2.0/?api_key=e65449d181214f936368984d4f4d4ae8&format=json&method=artist.getTopAlbums&mbid=f9b593e6-4503-414c-99a0-46595ecd2e23")
    suspend fun getTopAlbums(): Response<TopAlbumsResponse>
}