package com.example.musicapp.data.remote.library

import retrofit2.http.GET

interface ApiService {
    @GET("Remote_audio.json")
    suspend fun getSongs(): List<SongResponse>
}