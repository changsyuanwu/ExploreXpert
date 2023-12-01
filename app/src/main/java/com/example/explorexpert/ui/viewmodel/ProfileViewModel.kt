package com.example.explorexpert.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.User
import com.example.explorexpert.data.repository.FirebaseStorageRepository
import com.example.explorexpert.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepo: UserRepository,
    private val firebaseStorageRepo: FirebaseStorageRepository,
) : ViewModel() {

    private val mutableCurrentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = mutableCurrentUser

    private val tempProfilePhotoUrls = mutableListOf<String>()

    companion object {
        const val TAG = "ProfileViewModel"
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            if (auth.currentUser!= null) {
                mutableCurrentUser.value = userRepo.getUserById(auth.currentUser!!.uid)
            }
        }
    }

    private suspend fun uploadUserProfilePicture(newProfilePhotoUri: Uri): String? {
        if (currentUser.value != null) {
            val currentUserSnapshot = currentUser.value!!

            currentUserSnapshot.profilePictureURL?.let { url ->
                firebaseStorageRepo.deleteFile(url)
            }

            val uploadTask = viewModelScope.async {
                try {
                    firebaseStorageRepo.uploadFile(
                        "profile_pictures",
                        newProfilePhotoUri
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error uploading new profile picture: ${e.message}", e)
                    null
                }
            }
            val imagePath = uploadTask.await()
            return imagePath
        }
        else {
            return null
        }
    }

    fun updateUserProfilePicture(newProfilePhotoUri: Uri) {
        if (currentUser.value != null) {
            viewModelScope.launch {
                val currentUserSnapshot = currentUser.value!!

                val uploadedNewProfilePhotoUrl = uploadUserProfilePicture(newProfilePhotoUri)

                val newUser = currentUserSnapshot.copy(
                    profilePictureURL = uploadedNewProfilePhotoUrl
                )

                try {
                    userRepo.setUser(newUser)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating user profile photo: ${e.message}", e)
                }

                refreshCurrentUser()
            }
        }
    }

    fun updateUser(newFirstName: String, newLastName: String) {
        refreshCurrentUser()

        if (currentUser.value != null) {
            val currentUserSnapshot = currentUser.value!!

            // If the first name and last name are not empty
            if (newFirstName != "" && newLastName != "") {
                viewModelScope.launch {
                    val newUser = currentUserSnapshot.copy(
                        firstName = newFirstName,
                        lastName = newLastName,
                    )

                    try {
                        userRepo.setUser(newUser)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating user profile: ${e.message}", e)
                    }
                }
            }
        }
    }
}