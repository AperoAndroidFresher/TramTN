package com.example.musicapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.musicapp.R
import com.example.musicapp.base.BaseActivity

class LoginActivity : BaseActivity() {

    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var usernameError: TextView
    private lateinit var passwordError: TextView
    private lateinit var loginButton: Button
    private lateinit var signupText: TextView
    private lateinit var changeLanguageButton: Button

    private val userList = hashMapOf(
        "tram" to "Tram123"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        changeLanguageButton = findViewById(R.id.btnChangeLanguage)
        changeLanguageButton.setOnClickListener {
            showLanguageSelectionDialog()
        }

        usernameField = findViewById(R.id.ttUsername)
        passwordField = findViewById(R.id.ttPassword)
        usernameError = findViewById(R.id.txtUsernameError)
        passwordError = findViewById(R.id.txtPasswordError)
        loginButton = findViewById(R.id.btnLogin)
        signupText = findViewById(R.id.txtSignup)

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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        signupText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "Tiếng Việt", "Russia")
        val languageCodes = arrayOf("en", "vi", "ru")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Chọn ngôn ngữ")
        builder.setItems(languages) { _, which ->
            val selectedLanguageCode = languageCodes[which]
            setLocale(selectedLanguageCode)
            recreate()
        }
        builder.show()
    }


}
