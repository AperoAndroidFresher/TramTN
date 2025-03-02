package com.example.musicapp.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.dao.UserDao
import com.example.musicapp.data.local.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpViewModel(private val userDao: UserDao) : ViewModel() {

    fun signUp(username: String, password: String, email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = User(username = username, password = password, email = email,avatar = "")
            val userId = userDao.insertUser(user)
            withContext(Dispatchers.Main) {
                onResult(userId != -1L)
            }
        }
    }
}