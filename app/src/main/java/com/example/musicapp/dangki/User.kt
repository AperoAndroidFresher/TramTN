package com.example.musicapp.dangki

fun main(){
    val user = User(
        username = "user123",
        email = "test@apero.vn",
        password = "Password123",
        phoneNumber = "01234567890"
    )

}

class User(
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String
)
