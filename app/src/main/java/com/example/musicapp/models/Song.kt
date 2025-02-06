package com.example.musicapp.models

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val songUri: String,
    val albumArt: Bitmap?,
    val duration: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readParcelable(Bitmap::class.java.classLoader),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeString(songUri)
        parcel.writeParcelable(albumArt, flags)
        parcel.writeLong(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}
