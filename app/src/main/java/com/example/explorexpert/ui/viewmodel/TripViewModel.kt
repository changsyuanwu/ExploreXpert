package com.example.explorexpert.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.DateTimeRange
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class TripViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
    private val userRepo: UserRepository,
) : ViewModel() {

    companion object {
        const val TAG = "TripViewModel"
    }

    private val mutableSavedItems = MutableLiveData<List<SavedItem>>()
    val savedItems: LiveData<List<SavedItem>> get() = mutableSavedItems


    private val mutableTrip = MutableLiveData<Trip>()
    val trip: LiveData<Trip> get() = mutableTrip


    fun fetchSavedItems() {
        viewModelScope.launch {
            if (trip.value != null) {
                val savedItemsToDisplay = tripRepo.getSavedItemsFromTrip(trip.value!!)
                mutableSavedItems.value = savedItemsToDisplay
            }
        }

    }

    fun refreshTrip() {
        viewModelScope.launch {
            if (trip.value != null) {
                val updatedTrip = tripRepo.getTripById(trip.value!!.id)
                if (updatedTrip != null) {
                    setTrip(updatedTrip)
                    fetchSavedItems()
                }
            }
        }
    }

    fun setTrip(tripToSet: Trip) {
        mutableTrip.value = tripToSet
    }

    suspend fun getOwnerUserName(ownerUserId: String): String {
        if (auth.currentUser != null) {
            val owner = userRepo.getUserById(ownerUserId)

            if (owner != null) {
                return owner.firstName + " " + owner.lastName
            }
        }
        return ""
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            tripRepo.deleteTrip(tripId)
        }
    }

    fun getCurrentUserId() : String {
        if (auth.currentUser != null) {
            return auth.currentUser!!.uid
        }
        return ""
    }

    fun updateTrip(newTripName: String, isPrivate: Boolean) {
        refreshTrip()

        if (trip.value != null) {
            val newTrip = trip.value!!.copy(
                name = newTripName,
                private = isPrivate,
            )

            viewModelScope.launch {
                try {
                    tripRepo.setTrip(newTrip)
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error updating trip name and privacy: ${e.message}", e)
                }
            }
        }
    }

    fun updateTripDates(datesSelected: DateTimeRange) {
        refreshTrip()

        if (trip.value != null) {
            val newTrip = trip.value!!.copy(
                datesSelected = datesSelected
            )

            viewModelScope.launch {
                try {
                    tripRepo.setTrip(newTrip)
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error updating trip with dates: ${e.message}", e)
                }
            }
        }
    }
}