package com.example.musicapp.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.interfaces.OnSongClickListener
import com.example.musicapp.models.Song
import java.io.File

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClickListener: OnSongClickListener
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.songTitle.text = song.title
        holder.songArtist.text = song.artist

        val options = BitmapFactory.Options().apply {
            inSampleSize = 4
        }
        if (song.albumArt != null && File(song.albumArt).exists()) {
            val bitmap = BitmapFactory.decodeFile(song.albumArt, options)
            holder.songImage.setImageBitmap(bitmap)
        } else {
            holder.songImage.setImageResource(R.drawable.img_default_music_art)
        }

        holder.itemView.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "Playing: ${song.title} by ${song.artist}",
                Toast.LENGTH_SHORT
            ).show()
            onSongClickListener.onSongClick(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songImage: ImageView = itemView.findViewById(R.id.songImage)
        val songTitle: TextView = itemView.findViewById(R.id.songTitle)
        val songArtist: TextView = itemView.findViewById(R.id.songAuthor)
    }
}
