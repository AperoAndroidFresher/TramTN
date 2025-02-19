package com.example.musicapp.ui.home

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.remote.home.response.Album
import com.example.musicapp.data.remote.home.RetrofitClient
import com.example.musicapp.data.remote.home.response.Artist
import com.example.musicapp.data.remote.home.response.TopAlbumsResponse
import com.example.musicapp.data.remote.home.response.TopArtistsResponse
import com.example.musicapp.data.remote.home.response.TopTracksResponse
import com.example.musicapp.data.remote.home.response.Track
import com.example.musicapp.databinding.FragmentHomeBinding
import com.example.musicapp.ui.home.adapter.AlbumAdapter
import com.example.musicapp.ui.home.adapter.ArtistAdapter
import com.example.musicapp.ui.home.adapter.TrackAdapter
import com.example.musicapp.ui.setting.ProfileActivity
import com.example.musicapp.ui.setting.SettingFragment
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var albumAdapter: AlbumAdapter
    private val albumList = mutableListOf<Album>()
    private val fullAlbumList = mutableListOf<Album>()

    private val trackList = mutableListOf<Track>()
    private val fullTrackList = mutableListOf<Track>()
    private lateinit var trackAdapter: TrackAdapter

    private val artistList = mutableListOf<Artist>()
    private val fullArtistList = mutableListOf<Artist>()
    private lateinit var artistAdapter: ArtistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", "")
        binding.username.text = savedUsername

        setupRecyclerView()
        setupTopTracksRecyclerView()
        setupTopArtistsRecyclerView()

        fetchAlbumsFromApi()
        fetchTopTracksFromApi()
        fetchTopArtistsFromApi()

        binding.icSetting.setOnClickListener {
            openSettingFragment()
        }
        binding.avatar.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.seeAllAlbums.setOnClickListener {
            openSeeAllScreen("Top Albums", "ALBUM", fullAlbumList)
        }
        binding.seeAllTracks.setOnClickListener {
            openSeeAllScreen("Top Tracks", "TRACK", fullTrackList)
        }
        binding.seeAllArtists.setOnClickListener {
            openSeeAllScreen("Top Artists", "ARTIST", fullArtistList)
        }
    }
    private fun showErrorLayout() {
        binding.layoutError.visibility = View.VISIBLE
    }

    private fun hideErrorLayout() {
        binding.layoutError.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun openSettingFragment() {
        val settingFragment = SettingFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragContainer, settingFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupRecyclerView() {
        albumAdapter = AlbumAdapter(albumList)
        binding.recyclerAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerAlbums.adapter = albumAdapter
    }
    private fun setupTopTracksRecyclerView() {
        trackAdapter = TrackAdapter(trackList)
        binding.recyclerTracks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTracks.adapter = trackAdapter
//        val spaceInPixels = resources.getDimensionPixelSize(R.dimen.recycler_item_spacing)
//        binding.recyclerTracks.addItemDecoration(HorizontalSpaceItemDecoration(spaceInPixels))
    }
    private fun setupTopArtistsRecyclerView() {
        artistAdapter = ArtistAdapter(artistList)
        binding.recyclerArtists.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerArtists.adapter = artistAdapter
        val spaceInPixels = resources.getDimensionPixelSize(R.dimen.recycler_item_spacing)
        binding.recyclerArtists.addItemDecoration(HorizontalSpaceItemDecoration(spaceInPixels))
    }

    private fun fetchAlbumsFromApi() {
        val apiKey = "e65449d181214f936368984d4f4d4ae8"
        val mbid = "381086ea-f511-4aba-bdf9-71c753dc5077"

        RetrofitClient.instance.getTopAlbums(apiKey = apiKey, mbid = mbid)
            .enqueue(object : Callback<TopAlbumsResponse> {
                override fun onResponse(call: Call<TopAlbumsResponse>, response: Response<TopAlbumsResponse>) {
                    if (response.isSuccessful) {
                        val albums = response.body()?.topAlbums?.album ?: emptyList()

                        fullAlbumList.clear()
                        fullAlbumList.addAll(albums)

                        val limitedAlbums = albums.take(6)
                        albumList.clear()
                        albumList.addAll(limitedAlbums)
                        albumAdapter.notifyDataSetChanged()
                        Log.d("HomeFragment", "Albums fetched: ${albumList.size}")

                    } else {
                        Toast.makeText(requireContext(), "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TopAlbumsResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Lỗi: ${t.message}")
                    Toast.makeText(requireContext(), "Lỗi kết nối API!", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun fetchTopTracksFromApi() {
        val apiKey = "e65449d181214f936368984d4f4d4ae8"
        val mbid = "381086ea-f511-4aba-bdf9-71c753dc5077"

        RetrofitClient.instance.getTopTracks(apiKey = apiKey, mbid = mbid)
            .enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                    if (response.isSuccessful) {
                        val tracks = response.body()?.topTracks?.track ?: emptyList()

                        fullTrackList.clear()
                        fullTrackList.addAll(tracks)

                        val limitedTracks = tracks.take(5)
                        trackList.clear()
                        trackList.addAll(limitedTracks)
                        trackAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "Lỗi tải dữ liệu Top Tracks!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi kết nối API Top Tracks!", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun fetchTopArtistsFromApi() {
        val apiKey = "e65449d181214f936368984d4f4d4ae8"

        RetrofitClient.instance.getTopArtists(apiKey = apiKey)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        Log.d("HomeFragment", "API Response: $responseBody")
                        val artists = responseBody?.topArtists?.artist ?: emptyList()

                        fullArtistList.clear()
                        fullArtistList.addAll(artists)

                        val limitedArtists = artists.take(5)
                        artistList.clear()
                        artistList.addAll(limitedArtists)
                        artistAdapter.notifyDataSetChanged()
                        Log.d("HomeFragment", "Artist fetched: ${artistList.size}")
                    } else {
                        Toast.makeText(requireContext(), "Lỗi tải dữ liệu Top Artists!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi kết nối API Top Artists!", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun openSeeAllScreen(title: String, type: String, data: List<Any>) {
        val intent = Intent(requireContext(), SeeAllActivity::class.java)
        intent.putExtra("TITLE", title)
        intent.putExtra("TYPE", type)
        val jsonData = Gson().toJson(data)
        intent.putExtra("DATA", jsonData)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        val sharedPref = requireContext().getSharedPreferences("UserSession", MODE_PRIVATE)
        val avatarPath = sharedPref.getString("avatar", "")
        if (!avatarPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarPath)
                .circleCrop()
                .into(binding.avatar)
        }else {
            binding.avatar.setImageResource(R.drawable.img_no_image)
        }
    }
}
