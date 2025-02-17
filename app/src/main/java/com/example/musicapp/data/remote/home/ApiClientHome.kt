package com.example.musicapp.data.remote.home

import com.example.musicapp.data.remote.library.ApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClientHome {
    private const val BASE_URL = "https://ws.audioscrobbler.com/"

    private val retrofit by lazy { buildRetrofit() }

    val apiService: ApiServiceHome = retrofit.create(ApiServiceHome::class.java)

    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
}
