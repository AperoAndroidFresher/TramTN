package com.example.musicapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.R
import com.example.musicapp.ui.main.MainActivity
import com.example.musicapp.ui.signup.SignUpActivity
import com.example.musicapp.base.BaseActivity
import com.example.musicapp.data.local.database.AppDatabase

class LoginActivity : BaseActivity() {

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var usernameError: TextView
    private lateinit var passwordError: TextView
    private lateinit var loginButton: Button
    private lateinit var signupText: TextView
    private lateinit var rememberMeCheckBox: CheckBox

    private val loginViewModel: LoginViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val userDao = AppDatabase.getDatabase(applicationContext).userDao()
                return LoginViewModel(userDao) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameField = findViewById(R.id.ttUsername)
        passwordField = findViewById(R.id.ttPassword)
        usernameError = findViewById(R.id.txtUsernameError)
        passwordError = findViewById(R.id.txtPasswordError)
        loginButton = findViewById(R.id.btnLogin)
        signupText = findViewById(R.id.txtSignup)
        rememberMeCheckBox = findViewById(R.id.chkRememberMe)

        loadRememberedUser()

        loginButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            var isValid = true
            usernameError.visibility = View.GONE
            passwordError.visibility = View.GONE

            if (username.isEmpty()) {
                usernameError.text = "Username is required"
                usernameError.visibility = View.VISIBLE
                isValid = false
            }

            if (password.isEmpty()) {
                passwordError.text = "Password is required"
                passwordError.visibility = View.VISIBLE
                isValid = false
            }

            if (isValid) {
                loginViewModel.login(username, password) { success, user ->
                    if (success && user != null) {
                        saveUserSession(user.username, user.userId, user.email, user.avatar, rememberMeCheckBox.isChecked)

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        passwordError.text = "Invalid username or password"
                        passwordError.visibility = View.VISIBLE
                    }
                }


            }
        }

        signupText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserSession(username: String, userId: Int,userEmail : String, avatar: String?, rememberMe: Boolean) {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("username"  , username)
        editor.putInt("userId", userId)
        editor.putString("email", userEmail)
        editor.putString("avatar", avatar)

        if (rememberMe) {
            editor.putBoolean("rememberMe", true)
        } else {
            editor.remove("rememberMe")
        }

        editor.apply()
    }

    private fun loadRememberedUser() {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", "")
        val rememberMe = sharedPref.getBoolean("rememberMe", false)

        if (rememberMe) {
            usernameField.setText(savedUsername)
            rememberMeCheckBox.isChecked = true
        }
    }
}


