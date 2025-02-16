package com.example.musicapp.data.remote.library

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://static.apero.vn/techtrek/"

    private val retrofit by lazy { buildRetrofit() }

    fun build(): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(buildClient())
            .addConverterFactory(GsonConverterFactory.create(gsonConfig))
            .build()
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    private val gsonConfig = GsonBuilder().create()
}