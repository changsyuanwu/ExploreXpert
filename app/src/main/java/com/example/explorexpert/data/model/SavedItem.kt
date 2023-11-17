package com.example.explorexpert.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

data class SavedItem(
    val id: String = UUID.randomUUID().toString(),
    val type: SavedItemType = SavedItemType.BLANK,
    val imgURL: String = "",
    val title: String = "",
    val description: String = "",
    val placeId: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    var updatedAt: Timestamp? = null,
)
