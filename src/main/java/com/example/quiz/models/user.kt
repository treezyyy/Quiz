package com.example.quiz.model

data class User(
    val id: Int = 0,
    val email: String,
    val password: String,
    val role: String,
    val isVerified: Boolean = false,
    val penguins: Int = 0 // Add currency field
)