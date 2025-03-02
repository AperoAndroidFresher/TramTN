package com.example.musicapp.ui.signup

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicapp.R
import com.example.musicapp.base.BaseActivity
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.ui.login.LoginActivity


class SignUpActivity : BaseActivity() {

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var emailField: EditText
    private lateinit var signUpButton: Button
    private lateinit var usernameErrorText: TextView
    private lateinit var passwordErrorText: TextView
    private lateinit var emailErrorText: TextView
    private lateinit var backArrow: ImageView
    private val signUpViewModel: SignUpViewModel by viewModels() {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val userDao = AppDatabase.getDatabase(applicationContext).userDao()
                return SignUpViewModel(userDao) as T
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        usernameField = findViewById(R.id.ttUsername)
        passwordField = findViewById(R.id.ttPassword)
        confirmPasswordField = findViewById(R.id.ttRePassword)
        emailField = findViewById(R.id.ttEmail)
        signUpButton = findViewById(R.id.btnSignup)
        usernameErrorText = findViewById(R.id.txtUsernameError)
        passwordErrorText = findViewById(R.id.txtPasswordError)
        emailErrorText = findViewById(R.id.txtEmailError)
        backArrow = findViewById(R.id.icBack)

        backArrow.setOnClickListener {
            finish()
        }
        signUpButton.setOnClickListener {
            finish()
        }


        signUpButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()
            val email = emailField.text.toString().trim()


            usernameErrorText.visibility = TextView.GONE
            passwordErrorText.visibility = TextView.GONE
            emailErrorText.visibility = TextView.GONE

            var isValid = true


            val usernamePattern = "^[a-z0-9]+$".toRegex()
            if (!username.matches(usernamePattern)) {
                usernameErrorText.text = "Invalid format"
                usernameErrorText.visibility = TextView.VISIBLE
                isValid = false
            }


            val passwordPattern = "^[a-zA-Z0-9]+$".toRegex()
            if (!password.matches(passwordPattern)) {
                passwordErrorText.visibility = TextView.VISIBLE
                isValid = false
            }


            if (password != confirmPassword) {
                passwordErrorText.text = "Passwords do not match"
                passwordErrorText.visibility = TextView.VISIBLE
                isValid = false
            }


            val emailPattern = "^[a-z0-9._-]+@apero\\.vn$".toRegex()
            if (!email.matches(emailPattern)) {
                emailErrorText.text = "Invalid format"
                emailErrorText.visibility = TextView.VISIBLE
                isValid = false
            }
            if (isValid) {
                signUpViewModel.signUp(username, password, email) { success ->
                    if (success) {
                        showToast("Sign up successful")
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showToast("Sign up failed")
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
