package com.example.explorexpert.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.User
import com.example.explorexpert.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepo: UserRepository,
) : ViewModel() {

    private val mutableCurrentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = mutableCurrentUser

    companion object {
        const val TAG = "ProfileViewModel"
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            mutableCurrentUser.value = userRepo.getUserById(auth.currentUser!!.uid)
            Log.d(TAG, currentUser.value.toString())
        }
    }

    fun updateUser(newFirstName: String, newLastName: String) {
        refreshCurrentUser()

        if (currentUser.value != null) {
            val currentUserSnapshot = currentUser.value!!

            // If either the first name or last name was changed and is not empty
            if (
                (newFirstName != currentUserSnapshot.firstName && newFirstName != "")
                || (newLastName != currentUserSnapshot.lastName && newLastName != "")
            ) {

            }

            val newUser = currentUserSnapshot.copy(
                firstName = newFirstName,
                lastName = newLastName,
            )

            viewModelScope.launch {
                try {
                    userRepo.setUser(newUser)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating user profile: ${e.message}", e)
                }
            }
        }
    }
}