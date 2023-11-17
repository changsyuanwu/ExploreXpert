package com.example.explorexpert.data.implementation

import android.util.Log
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripRepoImplementation @Inject constructor(
    db: FirebaseFirestore
) : TripRepository {

    companion object {
        private const val TAG = "TripRepository"
    }

    private val tripCollection = db.collection("trips")
    private val savedItemCollection = db.collection("savedItems")

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
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
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

    override suspend fun getTripById(tripId: String): Trip? {
        return withContext(Dispatchers.IO) {
            val tripDoc = tripCollection.document(tripId).get().await()

            try {
                val trip = tripDoc.toObject(Trip::class.java)
                return@withContext trip
            } catch (e: Exception) {
                Log.e(TAG, "Error casting document to trip object: ${e.message}")
                null
            }
        }
    }

    private suspend fun saveItemToCollection(savedItem: SavedItem): String {
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<String>()

            savedItemCollection.document(savedItem.id)
                .set(savedItem)
                .addOnSuccessListener {
                    Log.d(TAG, "Saved an item with id ${savedItem.id}")
                    deferred.complete(savedItem.id)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to save an item with id ${savedItem.id}", e)
                    deferred.completeExceptionally(e)
                }

            deferred.await()
        }
    }

    override suspend fun addItemToTrip(itemToAdd: SavedItem, tripToAddTo: Trip): Trip? {
        return withContext(Dispatchers.IO) {
            lateinit var itemId: String
            try {
                itemId = saveItemToCollection(itemToAdd)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to create a saved item with id ${itemToAdd.id}", e)
                return@withContext null
            }

            val deferred = CompletableDeferred<Trip>()

            tripToAddTo.savedItemIds.add(itemId)

            tripCollection.document(tripToAddTo.id)
                .set(tripToAddTo)
                .addOnSuccessListener {
                    Log.d(TAG, "Updated trip (${tripToAddTo.id}) with item (${itemId})")
                    deferred.complete(tripToAddTo)
                }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG,
                        "Failed to update trip (${tripToAddTo.id}) after creating item (${itemId})",
                        e
                    )
                    deferred.completeExceptionally(e)
                }
            deferred.await()
        }
    }

    private suspend fun getSavedItemById(savedItemId: String): SavedItem? {
        return withContext(Dispatchers.IO) {
            val savedItemDoc = savedItemCollection.document(savedItemId).get().await()

            try {
                val savedItem = savedItemDoc.toObject(SavedItem::class.java)
                return@withContext savedItem
            } catch (e: Exception) {
                Log.e(TAG, "Error casting document to saved item object: ${e.message}")
                null
            }
        }
    }

    override suspend fun getSavedItemsFromTrip(trip: Trip): List<SavedItem> {
        return withContext(Dispatchers.IO) {
            return@withContext trip.savedItemIds
                .mapNotNull { item ->
                    getSavedItemById(item)
                }.sortedByDescending { item ->
                    item.createdAt
                }
        }
    }

    override suspend fun getSavedItemsByUserId(userId: String): List<SavedItem> =
        withContext(Dispatchers.IO) {
            try {
                val ownedItemsQueryResult = savedItemCollection
                    .whereEqualTo("ownerUserId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val ownedItems = ownedItemsQueryResult.documents.mapNotNull { document ->
                    try {
                        val item = document.toObject(SavedItem::class.java)
                        item?.id = document.id
                        item
                    } catch (e: Exception) {
                        Log.e(TAG, "Error casting document to saved item object: ${e.message}")
                        null
                    }
                }

                return@withContext ownedItems
            } catch (e: Exception) {
                Log.e(TAG, "Error reading saved items query: ${e.message}")
                return@withContext emptyList()
            }
        }
}