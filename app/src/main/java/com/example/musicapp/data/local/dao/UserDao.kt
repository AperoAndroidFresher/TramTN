package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.musicapp.data.local.entity.User

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User):Long

    @Query("SELECT * FROM Users WHERE (username = :username or email = :username) AND password = :password")
    fun checkUser(username: String, password: String): User?
}