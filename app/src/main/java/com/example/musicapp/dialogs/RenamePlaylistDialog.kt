package com.example.musicapp.dialogs

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import com.example.musicapp.models.Playlist


fun showRenameDialog(context: Context, playlist: Playlist, onRenameConfirmed: (String) -> Unit) {
    val editText = EditText(context).apply {
        setText(playlist.title)
    }

    AlertDialog.Builder(context)
        .setTitle("Rename Playlist")
        .setView(editText)
        .setPositiveButton("OK") { _, _ ->
            val newTitle = editText.text.toString()
            if (newTitle.isNotBlank()) {
                onRenameConfirmed(newTitle)
            }
        }
        .setNegativeButton("Cancel", null)
        .show()
}
