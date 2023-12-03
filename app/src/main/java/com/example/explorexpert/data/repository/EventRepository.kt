package com.example.explorexpert.data.repository

import com.example.explorexpert.data.model.Event

interface EventRepository {
    suspend fun setEvent(event: Event): String

    suspend fun setEventWithDates(event: Event): String

    suspend fun getEventsByUserId(userId: String): List<Event>

    suspend fun getEventsByUserIdAndDate(userId: String, startDate: String): List<Event>

    suspend fun getEventById(eventId: String): Event?

    suspend fun deleteEvent(eventId: String)
}