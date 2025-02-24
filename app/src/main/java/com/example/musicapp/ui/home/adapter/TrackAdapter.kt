package com.example.musicapp.ui.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.data.remote.home.response.Track
import com.example.musicapp.databinding.ItemTrackBinding
import com.squareup.picasso.Picasso

class TrackAdapter(private val tracks: List<Track>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private val colorList = listOf(
        Color.parseColor("#FF8080"),
        Color.parseColor("#FFFF80"),
        Color.parseColor("#80FF80"),
        Color.parseColor("#80FFFF"),
        Color.parseColor("#8080FF"),
        Color.parseColor("#FF80FF"),
        Color.parseColor("#FF0000"),
        Color.parseColor("#C080FF")
    )
    class TrackViewHolder(val binding: ItemTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(track: Track, color: Int) {
            binding.tvTitle.text = track.name
            binding.tvArtistName.text = track.artist.name
            binding.tvListenerCount.text = "Playcount: ${track.playcount}"

            val imageUrl = track.image.find { it.size == "medium" }?.url ?: ""
            if (imageUrl.isNotEmpty()) {
                Picasso.get().load(imageUrl).into(binding.imgTrack)
            }else{
                binding.imgTrack.setImageResource(R.drawable.img_no_image)
            }
            binding.bottomBar.setBackgroundColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val color = colorList[position % colorList.size]
        holder.bind(tracks[position], color)
    }

    override fun getItemCount(): Int = tracks.size
}
