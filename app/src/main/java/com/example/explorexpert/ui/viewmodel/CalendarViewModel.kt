package com.example.explorexpert.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.CalendarEvent
import com.example.explorexpert.data.repository.CalendarEventRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalendarViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val calendarEventsRepo: CalendarEventRepository,
) : ViewModel() {

    companion object {
        const val TAG = "CalendarViewModel"
    }

    private val mutableCalendarEvents = MutableLiveData<List<CalendarEvent>>()
    val calendarEvents: LiveData<List<CalendarEvent>> get() = mutableCalendarEvents

    init {
        fetchCalendarEvents()
    }

    fun fetchCalendarEvents() {
        viewModelScope.launch {
            if (auth.currentUser != null) {
                val calendarEventsToDisplay = calendarEventsRepo.getCalendarEventsByUserId(auth.currentUser!!.uid)

                mutableCalendarEvents.value = (calendarEventsToDisplay)
            }
        }
    }
}