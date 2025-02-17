package com.example.musicapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.remote.home.response.Artist

class ArtistAdapter(private var artists: List<Artist>) : RecyclerView.Adapter<ArtistAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvArtistName)
        val image: ImageView = view.findViewById(R.id.imgArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = artists[position]
        holder.name.text = artist.name
        Glide.with(holder.image.context)
            .load(artist.imageUrl)
            .placeholder(R.drawable.img_default_music_art)
            .error(R.drawable.img_default_music_art)
            .into(holder.image)    }

    override fun getItemCount() = artists.size

    fun updateData(newData: List<Artist>) {
        this.artists = newData
        notifyDataSetChanged()
    }

}