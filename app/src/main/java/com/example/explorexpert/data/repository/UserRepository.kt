package com.example.explorexpert.data.repository

import com.example.explorexpert.data.model.User

interface UserRepository {

    suspend fun setUser(user: User): String

    suspend fun getUserById(userId: String): User?
}