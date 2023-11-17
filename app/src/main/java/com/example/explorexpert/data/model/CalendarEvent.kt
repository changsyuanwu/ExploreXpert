package com.example.explorexpert.data.model

import java.util.UUID

data class CalendarEvent(
    var id: String = UUID.randomUUID().toString(),
    var name: String? = "",
    var summary: String? = "",
    var startDate: String = "",
)
