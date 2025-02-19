package com.example.musicapp.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.data.remote.home.response.Album
import com.example.musicapp.data.remote.home.response.Artist
import com.example.musicapp.data.remote.home.response.Track
import com.example.musicapp.databinding.ActivitySeeAllBinding
import com.example.musicapp.ui.home.adapter.AlbumAdapter
import com.example.musicapp.ui.home.adapter.ArtistAdapter
import com.example.musicapp.ui.home.adapter.TrackAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SeeAllActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeAllBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("TITLE") ?: ""
        binding.tvTitleSeeAll.text = title

        val type = intent.getStringExtra("TYPE") ?: ""
        val jsonData = intent.getStringExtra("DATA") ?: ""

        when (type) {
            "ALBUM" -> {
                val listType = object : TypeToken<List<Album>>() {}.type
                val albumList: List<Album> = Gson().fromJson(jsonData, listType)
                val adapter = AlbumAdapter(albumList.toMutableList())
                binding.recyclerSeeAll.layoutManager = LinearLayoutManager(this)
                binding.recyclerSeeAll.adapter = adapter
            }
            "TRACK" -> {
                val listType = object : TypeToken<List<Track>>() {}.type
                val trackList: List<Track> = Gson().fromJson(jsonData, listType)
                val adapter = TrackAdapter(trackList.toMutableList())
                binding.recyclerSeeAll.layoutManager = GridLayoutManager(this, 2)
                binding.recyclerSeeAll.adapter = adapter
            }
            "ARTIST" -> {
                val listType = object : TypeToken<List<Artist>>() {}.type
                val artistList: List<Artist> = Gson().fromJson(jsonData, listType)
                val adapter = ArtistAdapter(artistList.toMutableList())
                binding.recyclerSeeAll.layoutManager = GridLayoutManager(this, 2)
                binding.recyclerSeeAll.adapter = adapter
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
