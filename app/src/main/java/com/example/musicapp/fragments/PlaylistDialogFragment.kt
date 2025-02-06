package com.example.musicapp.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.activities.MainActivity
import com.example.musicapp.adapters.PlaylistAdapter
import com.example.musicapp.databinding.FragmentPlaylistDialogBinding
import com.example.musicapp.models.Playlist
import com.example.musicapp.models.Song

class PlaylistDialogFragment : DialogFragment() {

    private var _binding: FragmentPlaylistDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var playlistAdapter: PlaylistAdapter
    private val playlists = mutableListOf<Playlist>()


    private var song: Song? = null
    private var listener: OnPlaylistSelectedListener? = null

    interface OnPlaylistSelectedListener {
        fun onPlaylistSelected(song: Song, playlist: Playlist)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OnPlaylistSelectedListener) {
            listener = parentFragment as OnPlaylistSelectedListener
        } else if (context is OnPlaylistSelectedListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        song = arguments?.getParcelable("song")

        if (requireActivity() is MainActivity) {
            playlists.clear()
            playlists.addAll((requireActivity() as MainActivity).getPlaylists())
        }

        setupRecyclerView()
        updateUI()

        binding.btnAddPlaylist.setOnClickListener {
            song?.let {
                showAddPlaylistDialog(requireContext(), it)
            }
        }


    }
    private fun showAddPlaylistDialog(context: Context, song: Song) {
        val input = EditText(context)
        input.hint = "Enter playlist name"

        val dialog = AlertDialog.Builder(context)
            .setTitle("Create New Playlist")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val playlistName = input.text.toString().trim()
                if (playlistName.isNotEmpty()) {
                    if (context is MainActivity) {
                        context.addPlaylist(playlistName, song)

                        playlists.clear()
                        playlists.addAll(context.getPlaylists())
                        playlistAdapter.notifyDataSetChanged()
                        updateUI()
                    }
                } else {
                    Toast.makeText(context, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }



    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(
            playlists,
            onRename = { playlist -> showRenameDialog(playlist) },
            onRemove = { playlist ->
                playlists.remove(playlist)
                playlistAdapter.updatePlaylists(playlists)
                updateUI()
            },
            onPlaylistClick = { playlist ->
                song?.let {
                    playlist.songs.add(it)
                    Toast.makeText(context, "Add song to '${playlist.title}'", Toast.LENGTH_SHORT).show()
                    listener?.onPlaylistSelected(it, playlist)
                    dismiss()
                }
            }

        )
        binding.recyclerViewPlaylists.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPlaylists.adapter = playlistAdapter
    }

    private fun updateUI() {
        if (playlists.isEmpty()) {
            binding.viewFlipper.displayedChild = 0
        } else {
            binding.viewFlipper.displayedChild = 1
        }
    }

    private fun showRenameDialog(playlist: Playlist) {
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Rename Playlist")

        val input = android.widget.EditText(context)
        input.setText(playlist.title)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newTitle = input.text.toString().trim()
            if (newTitle.isNotEmpty()) {
                playlist.title = newTitle
                playlistAdapter.updatePlaylists(playlists)
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#3E3E3E")))
    }


    companion object {
        fun newInstance(song: Song): PlaylistDialogFragment {
            val fragment = PlaylistDialogFragment()
            val args = Bundle()
            args.putParcelable("song", song)
            fragment.arguments = args
            return fragment
        }
    }
}
