package com.example.explorexpert.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.SavedItemType
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

    private val mutableSavedItems = MutableLiveData<List<SavedItem>>()
    val savedItems: LiveData<List<SavedItem>> get() = mutableSavedItems

    init {
        fetchTrips()
        fetchSavedItems()
    }

    fun fetchTrips() {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                val tripsToDisplay = tripRepo.getTripsByUserId(auth.currentUser!!.uid)
                mutableTrips.value = (tripsToDisplay)
            }
        }
    }

    fun fetchSavedItems() {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                val savedItemsToDisplay = tripRepo.getSavedItemsByUserId(auth.currentUser!!.uid)
                    .filter { item ->
                        item.type == SavedItemType.PLACE || item.type == SavedItemType.LINK
                    }

                mutableSavedItems.value = savedItemsToDisplay
            }
        }
    }

    fun getCurrentUserId() : String {
        if (auth.currentUser != null) {
            return auth.currentUser!!.uid
        }
        return ""
    }
}