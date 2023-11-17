package com.example.explorexpert.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.SavedItemType
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddTripItemViewModel@Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
) : ViewModel() {

    private lateinit var trip: Trip

    fun setTrip(tripToSet: Trip) {
        trip = tripToSet
    }

    fun addNote(title: String, description: String) {
        viewModelScope.launch {
            val item = SavedItem(type = SavedItemType.NOTE, title = title, description = description)
            tripRepo.addItemToTrip(item, trip)
        }
    }

    fun addPlace(place: Place) {
        viewModelScope.launch {
            val item = SavedItem(type = SavedItemType.PLACE, title = place.name.toString(), placeId = place.id.toString())
            tripRepo.addItemToTrip(item, trip)
        }
    }
}