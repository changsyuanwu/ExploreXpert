package com.example.explorexpert.data.implementation

import android.util.Log
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripRepoImplementation @Inject constructor(
    db: FirebaseFirestore
): TripRepository {

    companion object {
        private const val TAG = "TripRepository"
    }

    private val tripCollection = db.collection("trips")
    override suspend fun setTrip(trip: Trip): String {
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<String>()

            tripCollection.document(trip.id)
                .set(trip)
                .addOnSuccessListener {
                    Log.d(TAG, "Created a trip with id ${trip.id}")
                    deferred.complete(trip.id)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to create trip with id ${trip.id}", e)
                    deferred.completeExceptionally(e)
                }

            deferred.await()
        }
    }

    override suspend fun getTripsByUserId(userId: String): List<Trip> =
        withContext(Dispatchers.IO) {
            try {
                val ownedTripsQueryResult = tripCollection
                    .whereEqualTo("ownerUserId", userId)
                    .get()
                    .await()

                val ownedTrips = ownedTripsQueryResult.documents.mapNotNull { document ->
                    try {
                        val trip = document.toObject(Trip::class.java)
                        trip?.id = document.id
                        trip
                    } catch (e: Exception) {
                        Log.e(TAG, "Error casting document to trip object: ${e.message}")
                        null
                    }
                }

                // Add support for shared trips later
//                val sharedWithTripsQueryResult = tripCollection
//                    .where
//                    .get()
//                    .await()
//
//                val sharedWithTrips = sharedWithTripsQueryResult.documents.mapNotNull { document ->
//                    try {
//                        val trip = document.toObject(Trip::class.java)
//                        trip?.id = document.id
//                        trip
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Error casting document to trip object: ${e.message}")
//                        null
//                    }
//                }

                return@withContext ownedTrips
            } catch (e: Exception) {
                Log.e(TAG, "Error reading trips query: ${e.message}")
                return@withContext emptyList()
            }
        }

}