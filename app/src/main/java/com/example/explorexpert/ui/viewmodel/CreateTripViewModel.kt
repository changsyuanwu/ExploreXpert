package com.example.explorexpert.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateTripViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "CreateTripViewModel"
    }


    fun createTrip(tripName: String) {
        if (tripName != "" && auth.currentUser != null) {
            val trip = Trip(
                name = tripName,
                ownerUserId = auth.currentUser!!.uid
            )

            viewModelScope.launch {
                try {
                    tripRepo.setTrip(trip)
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error creating trip in collection: ${e.message}", e)
                }
            }
        }
    }
}