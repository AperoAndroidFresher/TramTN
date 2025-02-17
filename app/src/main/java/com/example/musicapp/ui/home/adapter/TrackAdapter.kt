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
import com.example.musicapp.data.remote.home.response.Track

class TrackAdapter(private var tracks: List<Track>) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvTitle)
        val artist: TextView = view.findViewById(R.id.tvArtistName)
        val image: ImageView = view.findViewById(R.id.imgArtist)
        val listenerCount: TextView = view.findViewById(R.id.tvListenerCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = tracks[position]
        holder.name.text = track.name
        holder.artist.text = track.artist
        holder.listenerCount.text = track.listenerCount.toString()
        Glide.with(holder.image.context).load(track.imageUrl).into(holder.image)
    }

    override fun getItemCount() = tracks.size

    fun updateData(newData: List<Track>) {
        this.tracks = newData
        notifyDataSetChanged()
    }
}