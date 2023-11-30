package com.example.explorexpert.data.implementation

import android.util.Log
import com.example.explorexpert.data.model.Event
import com.example.explorexpert.data.repository.EventRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EventRepoImplementation @Inject constructor(
    db: FirebaseFirestore
): EventRepository {

    companion object {
        private const val TAG = "EventRepository"
    }

    private val eventCollection = db.collection("events")
    override suspend fun setEvent(event: Event): String {
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<String>()

            eventCollection.document(event.id)
                .set(event)
                .addOnSuccessListener {
                    Log.d(TAG, "Created an Event with id ${event.id}")
                    deferred.complete(event.id)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to create Event with id ${event.id}", e)
                    deferred.completeExceptionally(e)
                }

            deferred.await()
        }
    }

    override suspend fun getEventsByUserId(userId: String): List<Event> =
        withContext(Dispatchers.IO) {
            try {
                val ownedEventsQueryResult = eventCollection
                    .whereEqualTo("ownerUserId", userId)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val ownedEvents = ownedEventsQueryResult.documents.mapNotNull { document ->
                    try {
                        val event = document.toObject(Event::class.java)
                        event?.id = document.id
                        event

                    } catch (e: Exception) {
                        Log.e(EventRepoImplementation.TAG, "Error casting document to Event object: ${e.message}")
                        null
                    }
                }

                return@withContext ownedEvents
            } catch (e: Exception) {
                Log.e(EventRepoImplementation.TAG, "Error reading Events query: ${e.message}")
                return@withContext emptyList()
            }
        }

    override suspend fun getEventsByUserIdAndStartDate(userId: String, startDate: String): List<Event> =
        withContext(Dispatchers.IO) {
            try {
                val ownedEventsQueryResult = eventCollection
                    .whereEqualTo("ownerUserId", userId)
                    .whereEqualTo("startDate", startDate)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val ownedEvents = ownedEventsQueryResult.documents.mapNotNull { document ->
                    try {
                        val event = document.toObject(Event::class.java)
                        event?.id = document.id
                        event

                    } catch (e: Exception) {
                        Log.e(EventRepoImplementation.TAG, "Error casting document to Event object: ${e.message}")
                        null
                    }
                }

                return@withContext ownedEvents
            } catch (e: Exception) {
                Log.e(EventRepoImplementation.TAG, "Error reading Events query: ${e.message}")
                return@withContext emptyList()
            }
        }

}