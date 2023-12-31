package com.example.explorexpert.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.Event
import com.example.explorexpert.data.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CalendarViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val eventRepo: EventRepository,
) : ViewModel() {

    companion object {
        const val TAG = "CalendarViewModel"
    }

    private val mutableEvents = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = mutableEvents

    init {
        // fetch current date
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        fetchEventsByDate(LocalDateTime.now().format(formatter))
    }

    fun fetchEvents() {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                val eventsToDisplay = eventRepo.getEventsByUserId(auth.currentUser!!.uid)
                mutableEvents.value = (eventsToDisplay)
            }
            Log.d(TAG, "Fetched events")
        }
    }

    fun fetchEventsByDate(startDate: String) {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                val eventsToDisplay = eventRepo.getEventsByUserIdAndDate(auth.currentUser!!.uid, startDate)
                mutableEvents.value = (eventsToDisplay)
            }
            Log.d(TAG, "Fetched events for ${startDate}")
        }
    }

    fun createEvent(eventName: String, startDate: String, endDate: String, associatedTripId: String? = null): String? {
        if (eventName != "" && auth.currentUser != null) {
            val event = Event(
                name = eventName,
                startDate = startDate,
                endDate = endDate,
                associatedTripId = associatedTripId,
                ownerUserId = auth.currentUser!!.uid,
            )

            viewModelScope.launch {
                try {
                    eventRepo.setEvent(event)
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error creating event in collection: ${e.message}", e)
                }
            }

            return event.id
        }
        return null
    }

}