package com.example.musicapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.adapter.SongAdapter
import com.example.musicapp.databinding.FragmentSortingBinding
import com.example.musicapp.base.listeners.OnSongClickListener
import com.example.musicapp.models.Song
import com.example.musicapp.utils.SongUtils
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import java.util.Collections

class SortingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var songs: MutableList<Song>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSortingBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerView
        val btnBack: ImageButton = binding.btnCancel
        val btnConfirm: ImageButton = binding.btnConfirm

        val isGridLayout = arguments?.getBoolean("isGridLayout", false) ?: false
        songs = SongUtils.getSongsFromDevice(requireContext()).toMutableList()

        songAdapter = SongAdapter(songs, object : OnSongClickListener {
            override fun onSongClick(song: Song) {
                Toast.makeText(requireContext(), "Đã chọn bài hát: ${song.title}", Toast.LENGTH_SHORT).show()
            }
        }, isGridLayout)
        recyclerView.layoutManager = if (isGridLayout) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
        recyclerView.adapter = songAdapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                    (if (isGridLayout) ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT else 0), 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition


                Collections.swap(songs, fromPosition, toPosition)
                songAdapter.notifyItemMoved(fromPosition, toPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        touchHelper.attachToRecyclerView(recyclerView)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnConfirm.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelableArrayList("sortedSongs", ArrayList(songs))
            }
            setFragmentResult("sorting_result", bundle)

            Toast.makeText(requireContext(), "Sắp xếp thành công", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }
}
