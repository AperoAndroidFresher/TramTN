package com.example.musicapp.data.remote.home

import com.example.musicapp.data.remote.home.response.TopAlbumsResponse
import com.example.musicapp.data.remote.home.response.TopArtistsResponse
import com.example.musicapp.data.remote.home.response.TopTracksResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {
    @GET(".")
    fun getTopAlbums(
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("method") method: String = "artist.getTopAlbums",
        @Query("mbid") mbid: String
    ): Call<TopAlbumsResponse>

    @GET(".")
    fun getTopTracks(
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("method") method: String = "artist.getTopTracks",
        @Query("mbid") mbid: String
    ): Call<TopTracksResponse>

    @GET(".")
    fun getTopArtists(
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("method") method: String = "chart.gettopartists"
    ): Call<TopArtistsResponse>
}