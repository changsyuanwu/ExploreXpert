package com.example.explorexpert.data.repository

import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.model.User

interface TripRepository {

    suspend fun setTrip(trip: Trip): String

    suspend fun getTripsByUserId(userId: String): List<Trip>

    suspend fun getTripById(tripId: String): Trip?

    suspend fun addItemToTrip(itemToAdd: SavedItem, tripToAddTo: Trip): Trip?

    suspend fun getSavedItemsFromTrip(trip: Trip): List<SavedItem>
}