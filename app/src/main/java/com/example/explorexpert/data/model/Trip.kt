package com.example.explorexpert.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

data class Trip(
    var id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val private: Boolean = true,
    val ownerUserId: String = "",
    val datesSelected: DateTimeRange? = null,
    val sharedUsers: MutableList<SharedUserRecord> = mutableListOf(),
    val savedItemIds: MutableList<String> = mutableListOf(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    var updatedAt: Timestamp? = null,
)