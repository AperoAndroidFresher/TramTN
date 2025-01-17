package com.example.musicapp.dangki

fun main(){
    val validateFields = ValidateFields()
    val user = User(
        username = "user123",
        email = "test@apero.vn",
        password = "Password123",
        phoneNumber = "01234567890"
    )
    val isValidUsername = validateFields.field1(user.username)
    val isValidEmail = validateFields.field2(user.email)
    val isValidPhoneNumber = validateFields.field3(user.phoneNumber)

    println(isValidUsername)
    println(isValidEmail)
    println(isValidPhoneNumber)


}

class User(
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String
)
class ValidateFields{
    fun field1(name: String): Boolean {
        val usernameRegex = Regex("^[a-zA-Z0-9]+\$")
        return name.matches(usernameRegex)
    }
    fun field2(email: String): Boolean {
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@apero\\.vn\$")
        return email.matches(emailRegex)
    }
    fun field3(phoneNumber: String): Boolean {
        val phoneRegex = Regex("^\\d{11}\$")
        return phoneNumber.matches(phoneRegex)
    }
}