package com.example.explorexpert.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class TripViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
) : ViewModel() {

    companion object {
        const val TAG = "TripViewModel"
    }

    private val mutableSavedItems = MutableLiveData<List<SavedItem>>()
    val savedItems: LiveData<List<SavedItem>> get() = mutableSavedItems

    private lateinit var trip: Trip


    fun fetchSavedItems() {
        viewModelScope.launch {
            if (::trip.isInitialized) {
                val savedItemsToDisplay = tripRepo.getSavedItemsFromTrip(trip)
                mutableSavedItems.value = savedItemsToDisplay
            }
        }

    }

    fun setTrip(tripToSet: Trip) {
        trip = tripToSet
    }

}