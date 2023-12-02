package com.example.explorexpert.data.model

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    var profilePictureURL: String? = null
)