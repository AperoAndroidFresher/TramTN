package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.musicapp.data.local.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM Users WHERE userId = :userId")
    suspend fun getUser(userId: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User) : Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM Users WHERE (username = :username or email = :username) AND password = :password")
    fun checkUser(username: String, password: String): User?

    @Query("""SELECT * FROM Users WHERE(username = :username or email = :username) AND password = :password""")
    suspend fun getUserIdByUsernameAndPassword(username: String, password: String):User?
}