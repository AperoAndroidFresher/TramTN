package com.example.musicapp.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.dao.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    fun login(username: String, password: String, onResult: (Boolean, Int?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()

                val userId = userDao.getUserIdByUsernameAndPassword(username, password)
                val duration = System.currentTimeMillis() - startTime

                if (duration > 2000) {
                    Log.e("LoginViewModel", "Query took too long: ${duration}ms")
                }

                withContext(Dispatchers.Main) {
                    if (userId != null) {
                        onResult(true, userId)
                    } else {
                        onResult(false, null)
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error during login query", e)
                withContext(Dispatchers.Main) {
                    onResult(false, null)
                }
            }
        }
    }
}

