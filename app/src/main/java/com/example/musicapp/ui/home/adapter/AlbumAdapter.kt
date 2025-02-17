package com.example.musicapp.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.remote.home.response.Album

class AlbumAdapter(private var albums: List<Album>) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.albumTitle)
        val artist: TextView = view.findViewById(R.id.albumAuthor)
        val image: ImageView = view.findViewById(R.id.imgAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = albums[position]
        holder.name.text = album.name
        holder.artist.text = album.artist
        Glide.with(holder.image.context).load(album.imageUrl).into(holder.image)
    }

    override fun getItemCount() = albums.size
    fun updateData(newData: List<Album>) {
        this.albums = newData
        notifyDataSetChanged()
    }
}