package com.example.musicapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicapp.data.local.dao.UserDao
import com.example.musicapp.data.local.entity.User

@Database(entities = [User::class], version = 2)
abstract class UserDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao

    companion object{
        @Volatile
        private var instance: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return instance ?: synchronized(this){
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                    )
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}