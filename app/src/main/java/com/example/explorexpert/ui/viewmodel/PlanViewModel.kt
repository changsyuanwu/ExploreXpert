package com.example.explorexpert.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlanViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
) : ViewModel() {

    companion object {
        const val TAG = "PlanViewModel"
    }

    private val mutableTrips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> get() = mutableTrips

    init {
        fetchTrips()
    }

    fun fetchTrips() {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                val tripsToDisplay = tripRepo.getTripsByUserId(auth.currentUser!!.uid)

                mutableTrips.value = (tripsToDisplay)
            }
        }
    }
}