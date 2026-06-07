package com.example.focusquest

data class User(
    val username: String,
    val email: String,
    val password: String,
    var xp: Int = 0,
    var level: Int = 1,
    var profileImage: String? = null
)
