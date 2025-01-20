package com.example.musicapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class SignUpActivity : AppCompatActivity() {

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var emailField: EditText
    private lateinit var signUpButton: Button
    private lateinit var usernameErrorText: TextView
    private lateinit var passwordErrorText: TextView
    private lateinit var emailErrorText: TextView
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin)


        usernameField = findViewById(R.id.username)
        passwordField = findViewById(R.id.password)
        confirmPasswordField = findViewById(R.id.repassword)
        emailField = findViewById(R.id.Email)
        signUpButton = findViewById(R.id.SignButton)
        usernameErrorText = findViewById(R.id.usernameErrorText)
        passwordErrorText = findViewById(R.id.passwordErrorText)
        emailErrorText = findViewById(R.id.emailErrorText)
        backArrow = findViewById(R.id.backArrow)

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
                showToast("Sign Up Successful!")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
