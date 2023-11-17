package com.example.explorexpert.data.repository

import com.example.explorexpert.data.model.CalendarEvent

interface CalendarEventRepository {
    suspend fun setCalendarEvent(calendarEvent: CalendarEvent): String

    suspend fun getCalendarEventsByUserId(userId: String): List<CalendarEvent>
}