package com.example.musicapp

import android.content.Intent
import android.os.Bundle

import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameField: EditText

    private lateinit var usernameError: TextView
    private lateinit var passwordError: TextView
    private lateinit var loginButton: Button
    private lateinit var signupText: TextView
    private lateinit var passwordField: EditText


    private val userList = hashMapOf(
        "tram" to "Tram123"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        usernameField = findViewById(R.id.username)
        passwordField = findViewById(R.id.password)
        usernameError = findViewById(R.id.usernameError)
        passwordError = findViewById(R.id.passwordError)
        loginButton = findViewById(R.id.loginButton)
        signupText = findViewById(R.id.signupText)


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

            if (!isValid) return@setOnClickListener

            if (!userList.containsKey(username)) {
                usernameError.text = "Username does not exist"
                usernameError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (userList[username] != password) {
                passwordError.text = "Incorrect password"
                passwordError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        }

        signupText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}

