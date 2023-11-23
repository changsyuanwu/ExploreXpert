package com.example.explorexpert.data.model

import java.util.UUID

data class CalendarEvent(
    var name: String? = "",
    var id: String = UUID.randomUUID().toString(),
    var summary: String? = "",
    var startDate: String = "",
)
