package com.example.explorexpert.data.implementation

import android.util.Log
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.EventRepository
import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class TripRepoImplementation @Inject constructor(
    db: FirebaseFirestore,
    private val eventRepo: EventRepository,
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
                    Log.d(TAG, "Created/Updated a trip with id ${trip.id}")
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

    private suspend fun getTripsWithSavedItem(savedItemId: String): List<Trip> =
        withContext(Dispatchers.IO) {
            try {
                val tripsWithSavedItemQueryResult = tripCollection
                    .whereArrayContains("savedItemIds", savedItemId)
                    .get()
                    .await()

                val tripsWithSavedItem = tripsWithSavedItemQueryResult.documents.mapNotNull { doc ->
                    try {
                        val trip = doc.toObject(Trip::class.java)
                        trip?.id = doc.id
                        trip
                    } catch (e: Exception) {
                        Log.e(TAG, "Error casting document to Trip object: ${e.message}")
                        null
                    }
                }

                return@withContext tripsWithSavedItem
            } catch (e: Exception) {
                Log.e(TAG, "Error reading trips with saved item query: ${e.message}")
                return@withContext emptyList()
            }
        }

    private suspend fun removeSavedItemFromTrip(trip: Trip, savedItemId: String) {
        withContext(Dispatchers.IO) {
            trip.savedItemIds.remove(savedItemId)
            tripCollection.document(trip.id)
                .set(trip)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error removing saved item from trip: ${e.message}")
                }
        }
    }


    override suspend fun removeSavedItem(savedItemId: String) {
        withContext(Dispatchers.IO) {
            try {
                val tripsWithSavedItem = getTripsWithSavedItem(savedItemId)

                // Delete the saved item from all associated trips
                tripsWithSavedItem.forEach { trip ->
                    removeSavedItemFromTrip(trip, savedItemId)
                }

                // Delete the saved item
                savedItemCollection
                    .document(savedItemId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.e(TAG, "Error removing saved item: ${e.message}")
            }
        }
    }

    override suspend fun deleteTrip(tripId: String) {
        withContext(Dispatchers.IO) {
            try {
                val trip = getTripById(tripId)

                // Don't execute if the trip document can't be found
                if (trip == null) {
                    return@withContext
                }

                // Delete the trip first so it doesn't show in UI anymore
                tripCollection
                    .document(tripId)
                    .delete()
                    .await()

                // Remove any saved items associated with the trip
                trip.savedItemIds.forEach { itemId ->
                    removeSavedItem(itemId)
                }

                // If the associated calendar event is not null, delete it
                trip.associatedCalendarEventId?.let { eventId ->
                    eventRepo.deleteEvent(eventId)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting trip: ${e.message}")
            }
        }
    }

    override suspend fun removeAssociatedEventFromTrip(tripId: String) {
        withContext(Dispatchers.IO) {
            try {
                val trip = getTripById(tripId)

                if (trip == null) {
                    return@withContext
                }

                val newTrip = trip.copy(
                    associatedCalendarEventId = null
                )

                setTrip(newTrip)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing associated trip id from trip: ${e.message}")
            }
        }
    }

    private suspend fun getRandomPublicTrip(): Trip? =
        withContext(Dispatchers.IO) {
            try {
                val randomUUID = UUID.randomUUID().toString()
                val closestTripQueryResult = tripCollection
                    .whereGreaterThanOrEqualTo("id", randomUUID)
                    .whereEqualTo("private", false)
                    .orderBy("id")
                    .limit(1)
                    .get()
                    .await()

                try {
                    val closestTrip = closestTripQueryResult
                        .documents
                        .first()
                        .toObject(Trip::class.java)
                    return@withContext closestTrip
                } catch (e: Exception) {
                    Log.e(TAG, "Error casting random query result to trip object: ${e.message}", e)
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error query random trip: ${e.message}", e)
                return@withContext null
            }
        }

    private suspend fun getNumPublicTrips(): Long? =
        withContext(Dispatchers.IO) {
            try {
                val countQuery = tripCollection
                    .whereEqualTo("private", false)
                    .count()

                val task = countQuery.get(AggregateSource.SERVER).await()

                return@withContext task.count
            } catch (e: Exception) {
                Log.e(TAG, "Error counting number of public trips: ${e.message}", e)
                return@withContext null
            }
        }

    private suspend fun getAllPublicTrips(currentUserId: String): List<Trip> =
        withContext(Dispatchers.IO) {
            try {
                // Get all public trips that do not belong to the current user
                val publicTripsQueryResult = tripCollection
                    .whereEqualTo("private", false)
                    .whereNotEqualTo("ownerUserId", currentUserId)
                    .get()
                    .await()

                val publicTrips = publicTripsQueryResult.documents.mapNotNull { doc ->
                    try {
                        val trip = doc.toObject(Trip::class.java)
                        trip?.id = doc.id
                        trip
                    } catch (e: Exception) {
                        Log.e(TAG, "Error casting document to Trip object: ${e.message}", e)
                        null
                    }
                }

                return@withContext publicTrips
            } catch (e: Exception) {
                Log.e(TAG, "Error getting public trips: ${e.message}", e)
                return@withContext emptyList()
            }
        }

    override suspend fun getRandomPublicTrips(
        numRandomTrips: Int,
        currentUserId: String
    ): List<Trip> =
        withContext(Dispatchers.IO) {
            try {
                val allPublicTrips = getAllPublicTrips(currentUserId)

                if (allPublicTrips.isEmpty()) {
                    return@withContext emptyList()
                }

                if (allPublicTrips.size < numRandomTrips) {
                    return@withContext allPublicTrips.shuffled()
                }

                return@withContext allPublicTrips
                    .shuffled()
                    .take(numRandomTrips)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting random public trips: ${e.message}", e)
                return@withContext emptyList()
            }
        }

    private suspend fun createCopiesOfSavedItems(
        savedItemsToCopy: List<SavedItem>,
        currentUserId: String
    ): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val copiedItemIds = savedItemsToCopy.map { itemToCopy ->
                    val newItem = itemToCopy.copy(
                        id = UUID.randomUUID().toString(),
                        ownerUserId = currentUserId,
                        createdAt = null,
                        updatedAt = null,
                    )
                    return@map saveItemToCollection(newItem)
                }

                return@withContext copiedItemIds
            }
            catch (e: Exception) {
                Log.e(TAG, "Error creating copies of saved items: ${e.message}", e)
                return@withContext emptyList()
            }
        }
    }

    override suspend fun createCopyOfTrip(
        newTripName: String,
        currentUserId: String,
        tripToCopy: Trip
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val savedItemsToCopy = getSavedItemsFromTrip(tripToCopy)

                val copiedSavedItemsIds = createCopiesOfSavedItems(savedItemsToCopy, currentUserId)

                val newTrip = Trip(
                    name = newTripName,
                    ownerUserId = currentUserId,
                    savedItemIds = copiedSavedItemsIds.toMutableList()
                )

                return@withContext setTrip(newTrip)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating copy of trip: ${e.message}", e)
                return@withContext null
            }
        }
    }
}