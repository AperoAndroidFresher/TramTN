package com.example.musicapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.remote.home.response.Artist

import com.example.musicapp.databinding.ItemArtistBinding
import com.squareup.picasso.Picasso

class ArtistAdapter(private val artists: List<Artist>) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(val binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        val binding = holder.binding

        binding.tvArtistName.text = artist.name

        val imageUrl = artist.image.find { it.size == "medium" }?.url ?: ""

        if (imageUrl.isNotEmpty()) {
            Picasso.get().load(imageUrl).into(binding.imgArtist)
        }else{
            binding.imgArtist.setImageResource(R.drawable.img_no_image)
        }
    }

    override fun getItemCount(): Int = artists.size
}
