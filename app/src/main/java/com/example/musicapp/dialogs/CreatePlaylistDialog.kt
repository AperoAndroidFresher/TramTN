package com.example.musicapp.dialogs

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.musicapp.R
import com.example.musicapp.models.Playlist

class CreatePlaylistDialog(
    private val initialTitle: String? = null,
    private val onCreate: (Playlist) -> Unit
) : DialogFragment() {

    private lateinit var editTextTitle: EditText
    private lateinit var btnCancel: Button
    private lateinit var btnCreate: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_create_playlist, container, false)

        editTextTitle = view.findViewById(R.id.etPlayList)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnCreate = view.findViewById(R.id.btnCreate)

        editTextTitle.setText(initialTitle)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnCreate.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                val newPlaylist = Playlist(
                    id = System.currentTimeMillis().toInt(),
                    title = title,
                    songs = mutableListOf()
                )
                onCreate(newPlaylist)
                dismiss()
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
