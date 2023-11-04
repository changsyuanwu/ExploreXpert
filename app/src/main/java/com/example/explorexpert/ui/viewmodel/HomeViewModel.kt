package com.example.explorexpert.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    fun logOut() {
        auth.signOut()
    }

    suspend fun getCurrentUserName(): String {
        if (auth.currentUser != null) {
            val currentUser = userRepo.getUserById(auth.currentUser!!.uid)
            if (currentUser != null) {
                return "${currentUser.firstName} ${currentUser.lastName}"
            }
        }
        return ""
    }

    fun getCurrentUserEmail(): String {
        if (auth.currentUser != null) {
            return auth.currentUser!!.email.toString()
        }
        return ""
    }
}