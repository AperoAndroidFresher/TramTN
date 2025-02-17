package com.example.musicapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.data.remote.home.ApiClientHome
import com.example.musicapp.databinding.FragmentHomeBinding
import com.example.musicapp.ui.home.adapter.AlbumAdapter
import com.example.musicapp.ui.home.adapter.ArtistAdapter
import com.example.musicapp.ui.home.adapter.TrackAdapter
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var albumAdapter: AlbumAdapter

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        fetchData()
    }

    private fun setupRecyclerViews() {
        binding.recyclerArtists.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTracks.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerAlbums.layoutManager = GridLayoutManager(context, 2)

        artistAdapter = ArtistAdapter(emptyList())
        trackAdapter = TrackAdapter(emptyList())
        albumAdapter = AlbumAdapter(emptyList())

        binding.recyclerArtists.adapter = artistAdapter
        binding.recyclerTracks.adapter = trackAdapter
        binding.recyclerAlbums.adapter = albumAdapter
    }


    private fun fetchData() {
        lifecycleScope.launch {
            try {
                val artistsResponse = ApiClientHome.apiService.getTopArtists()
                val tracksResponse = ApiClientHome.apiService.getTopTracks()
                val albumsResponse = ApiClientHome.apiService.getTopAlbums()

                if (artistsResponse.isSuccessful && tracksResponse.isSuccessful && albumsResponse.isSuccessful) {
                    val artists = artistsResponse.body()?.artists?.artist ?: emptyList()
                    val tracks = tracksResponse.body()?.tracks?.track ?: emptyList()
                    val albums = albumsResponse.body()?.albums?.album?.take(6) ?: emptyList()

                    artistAdapter.updateData(artists)
                    trackAdapter.updateData(tracks)
                    albumAdapter.updateData(albums)

                } else {
                    Log.e("HomeFragment", "API Error: ${artistsResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Exception: ${e.message}")
            }
        }
    }


}

