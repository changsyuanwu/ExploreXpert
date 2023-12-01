package com.example.explorexpert.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

data class Event(
    var name: String? = "",
    val ownerUserId: String = "",
    var id: String = UUID.randomUUID().toString(),
    var description: String? = "",
    var startDate: String = "",
    var endDate: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    var updatedAt: Timestamp? = null,
)
