package com.example.explorexpert.data.implementation

import android.util.Log
import com.example.explorexpert.data.model.CalendarEvent
import com.example.explorexpert.data.repository.CalendarEventRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalendarEventImplementation @Inject constructor(
    db: FirebaseFirestore
): CalendarEventRepository {

    companion object {
        private const val TAG = "CalendarEventRepository"
    }

    private val calendarEventCollection = db.collection("calendarEvents")
    override suspend fun setCalendarEvent(calendarEvent: CalendarEvent): String {
        return withContext(Dispatchers.IO) {
            val deferred = CompletableDeferred<String>()

            calendarEventCollection.document(calendarEvent.id)
                .set(calendarEvent)
                .addOnSuccessListener {
                    Log.d(TAG, "Created an calendarEvent with id ${calendarEvent.id}")
                    deferred.complete(calendarEvent.id)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to create calendarEvent with id ${calendarEvent.id}", e)
                    deferred.completeExceptionally(e)
                }

            deferred.await()
        }
    }

    override suspend fun getCalendarEventsByUserId(userId: String): List<CalendarEvent> =
        withContext(Dispatchers.IO) {
            try {
                val ownedCalendarEventsQueryResult = calendarEventCollection
                    .whereEqualTo("ownerUserId", userId)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val ownedCalendarEvents = ownedCalendarEventsQueryResult.documents.mapNotNull { document ->
                    try {
                        val calendarEvent = document.toObject(CalendarEvent::class.java)
                        calendarEvent?.id = document.id
                        calendarEvent
                    } catch (e: Exception) {
                        Log.e(TAG, "Error casting document to CalendarEvent object: ${e.message}")
                        null
                    }
                }

                return@withContext ownedCalendarEvents
            } catch (e: Exception) {
                Log.e(TAG, "Error reading calendarEvents query: ${e.message}")
                return@withContext emptyList()
            }
        }

}