package com.example.musicapp.ui.playlist.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.musicapp.databinding.DialogCreatePlaylistBinding
import com.example.musicapp.data.local.entity.Playlist

class CreatePlaylistDialog(
    private val userId: Int,
    private val initialTitle: String? = null,
    private val onCreate: (Playlist) -> Unit
) : DialogFragment() {

    private var _binding: DialogCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPlayList.setText(initialTitle)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnCreate.setOnClickListener {
            val title = binding.etPlayList.text.toString().trim()
            if (title.isNotEmpty()) {
                val newPlaylist = Playlist(
                    userId = userId,
                    title = title
                )
                onCreate(newPlaylist)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout((resources.displayMetrics.widthPixels * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
