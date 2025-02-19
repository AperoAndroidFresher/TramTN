package com.example.musicapp.ui.setting

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.base.BaseActivity
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.data.local.entity.User
import com.example.musicapp.databinding.ActivityProfileBinding
import com.example.musicapp.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(binding.imgAvatar)
                    binding.imgAvatar.tag = uri.toString()
                    val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("avatar", uri.toString())
                        apply()
                    }

                    binding.icTick.visibility = View.VISIBLE
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val username = sharedPref.getString("username", "")
        val email = sharedPref.getString("email", "")
        val avatarPath = sharedPref.getString("avatar", "")

        binding.etUsername.setText(username)
        binding.etEmail.setText(email)

        if (!avatarPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarPath)
                .circleCrop()
                .into(binding.imgAvatar)
        } else {
            binding.imgAvatar.setImageResource(R.drawable.img_no_image)
        }

        binding.icCamera.setOnClickListener {
            checkAndRequestPermission()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.icTick.setOnClickListener {
            val newUsername = binding.etUsername.text.toString()
            val newAvatar = binding.imgAvatar.tag?.toString() ?: ""
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("username", newUsername)
                putString("avatar", newAvatar)
                apply()
            }
            val currentUserId = sharedPref.getInt("userId", -1)
            if (currentUserId != -1) {
                val db = AppDatabase.getDatabase(this)
                val userDao = db.userDao()

                CoroutineScope(Dispatchers.IO).launch {
                    val user = userDao.getUser(currentUserId)
                    if (user != null) {
                        val updatedUser = user.copy(username = newUsername, avatar = newAvatar)
                        userDao.updateUser(updatedUser)
                    } else {
                        val newUser = User(
                            userId = currentUserId,
                            username = newUsername,
                            email = binding.etEmail.text.toString(),
                            password = "",
                            avatar = newAvatar
                        )
                        userDao.insertUser(newUser)
                    }
                }
            } else {
                Toast.makeText(this, "User ID không hợp lệ", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

        binding.tvLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                apply()
            }

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImageLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Cần quyền truy cập ảnh", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkAndRequestPermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

}
