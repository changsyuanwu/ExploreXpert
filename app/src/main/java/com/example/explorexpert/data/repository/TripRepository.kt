package com.example.explorexpert.data.repository

import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip

interface TripRepository {

    suspend fun setTrip(trip: Trip): String

    suspend fun getTripsByUserId(userId: String): List<Trip>

    suspend fun getTripById(tripId: String): Trip?

    suspend fun addItemToTrip(itemToAdd: SavedItem, tripToAddTo: Trip): Trip?

    suspend fun getSavedItemsFromTrip(trip: Trip): List<SavedItem>

    suspend fun getSavedItemsByUserId(userId: String): List<SavedItem>

    suspend fun removeSavedItem(savedItemId: String)

    suspend fun deleteTrip(tripId: String)

    suspend fun removeAssociatedEventFromTrip(tripId: String)

    suspend fun getRandomPublicTrips(numRandomTrips: Int, currentUserId: String): List<Trip>

    suspend fun createCopyOfTrip(newTripName: String, currentUserId: String, tripToCopy: Trip): String?
}