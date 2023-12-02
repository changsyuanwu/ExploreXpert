package com.example.explorexpert.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.NearbyPlace
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.model.User
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }

    private val mutableCurrentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = mutableCurrentUser

    private val mutableNearbyPlaces = MutableLiveData<List<NearbyPlace>>()
    val nearbyPlaces: LiveData<List<NearbyPlace>> get() = mutableNearbyPlaces

    private val mutablePublicTrips = MutableLiveData<List<Trip>>()
    val publicTrips: LiveData<List<Trip>> get() = mutablePublicTrips

    private val numPublicTripsToGet = 20

    fun logOut() {
        auth.signOut()
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            if (auth.currentUser!= null) {
                mutableCurrentUser.value = userRepo.getUserById(auth.currentUser!!.uid)
            }
        }
    }

    fun getCurrentUserName(): String {
        if (currentUser.value != null) {
            val currentUserSnapshot = currentUser.value
            if (currentUserSnapshot != null) {
                return "${currentUserSnapshot.firstName} ${currentUserSnapshot.lastName}"
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

    fun setNearbyPlaces(nearbyPlacesToSet: List<NearbyPlace>) {
        mutableNearbyPlaces.value = nearbyPlacesToSet
    }

    fun getRandomPublicTrips() {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                val randomPublicTrips = tripRepo.getRandomPublicTrips(
                    numPublicTripsToGet,
                    auth.currentUser!!.uid
                )
                mutablePublicTrips.value = randomPublicTrips
            }
        }
    }
}