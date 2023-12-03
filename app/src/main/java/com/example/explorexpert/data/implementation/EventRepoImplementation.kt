package com.example.explorexpert.data.implementation

import android.util.Log
import com.example.explorexpert.data.model.Event
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.EventRepository
import com.example.explorexpert.data.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EventRepoImplementation @Inject constructor(
    db: FirebaseFirestore,
): EventRepository {

    companion object {
        private const val TAG = "EventRepository"
    }

    private val eventCollection = db.collection("events")

    // Can't inject this or we have a cycle
    private val tripRepo = TripRepoImplementation(db, this)

    override suspend fun setEvent(event: Event): String {
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<String>()

            eventCollection.document(event.id)
                .set(event)
                .addOnSuccessListener {
                    Log.d(TAG, "Created/Updated an Event with id ${event.id}")
                    deferred.complete(event.id)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to create Event with id ${event.id}", e)
                    deferred.completeExceptionally(e)
                }

            deferred.await()
        }
    }

    override suspend fun setEventWithDates(event: Event): String {
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<String>()

            eventCollection.document(event.id)
                .set(event)
                .addOnSuccessListener {
                    Log.d(TAG, "Created/Updated an Event with id ${event.id}")
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

    override suspend fun getEventsByUserIdAndDate(userId: String, date: String): List<Event> =
        withContext(Dispatchers.IO) {
            try {
                val ownedEventsQueryResultStart = eventCollection
                    .whereEqualTo("ownerUserId", userId)
                    .whereLessThanOrEqualTo("startDate", date)
                    .orderBy("startDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val ownedEventsQueryResultEnd = eventCollection
                    .whereEqualTo("ownerUserId", userId)
                    .whereGreaterThanOrEqualTo("endDate", date)
                    .orderBy("endDate", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val ownedEventsStart = ownedEventsQueryResultStart.documents.mapNotNull { document ->
                        try {
                            val event = document.toObject(Event::class.java)
                            event?.id = document.id
                            event

                        } catch (e: Exception) {
                            Log.e(
                                EventRepoImplementation.TAG,
                                "Error casting document to Event object: ${e.message}"
                            )
                            null
                        }
                    }

                val ownedEventsEnd = ownedEventsQueryResultEnd.documents.mapNotNull { document ->
                    try {
                        val event = document.toObject(Event::class.java)
                        event?.id = document.id
                        event

                    } catch (e: Exception) {
                        Log.e(
                            EventRepoImplementation.TAG,
                            "Error casting document to Event object: ${e.message}"
                        )
                        null
                    }
                }

                return@withContext ownedEventsStart.intersect<Event>(ownedEventsEnd.toSet<Event>())
                    .toList<Event>().sortedByDescending { it.updatedAt }
            } catch (e: Exception) {
                Log.e(EventRepoImplementation.TAG, "Error reading Events query: ${e.message}")
                return@withContext emptyList()
            }
        }
    override suspend fun getEventById(eventId: String): Event? {
        return withContext(Dispatchers.IO) {
            val eventDoc = eventCollection.document(eventId).get().await()

            try {
                val event = eventDoc.toObject(Event::class.java)
                return@withContext event
            } catch (e: Exception) {
                Log.e(TAG, "Error casting document to event object: ${e.message}")
                null
            }
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        withContext(Dispatchers.IO) {
            try {
                val event = getEventById(eventId)

                // Don't execute if the event document can't be found
                if (event == null) {
                    return@withContext
                }

                // If there is a trip associated with the event,
                //   update it to have no event associated anymore
                if (event.associatedTripId != null) {
                    tripRepo.removeAssociatedEventFromTrip(event.associatedTripId)
                }

                eventCollection
                    .document(eventId)
                    .delete()
                    .await()
            }
            catch (e: Exception) {
                Log.e(TAG, "Error deleting event: ${e.message}")
            }
        }
    }

}