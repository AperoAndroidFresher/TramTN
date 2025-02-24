package com.example.musicapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.remote.home.response.Album
import com.example.musicapp.databinding.ItemAlbumBinding
import com.squareup.picasso.Picasso

class AlbumAdapter(private val albums: List<Album>) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        val binding = holder.binding

        binding.albumTitle.text = album.name
        binding.albumAuthor.text = album.artist.name

        val imageUrl = album.image.find { it.size == "medium" }?.url ?: ""

        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).into(binding.imgAlbum)
        }else{
            binding.imgAlbum.setImageResource(R.drawable.img_no_image)
        }
    }

    override fun getItemCount(): Int = albums.size
}
