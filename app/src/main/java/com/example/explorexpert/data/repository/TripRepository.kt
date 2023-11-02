package com.example.explorexpert.data.repository

import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.model.User

interface TripRepository {

    suspend fun setTrip(trip: Trip): String

}