package com.example.musicapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.dao.UserDao
import com.example.musicapp.data.local.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    fun login(username: String, password: String, onResult: (Boolean, User?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userDao.getUserIdByUsernameAndPassword(username, password)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        onResult(true, user)
                    } else {
                        onResult(false, null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, null)
                }
            }
        }
    }
}


