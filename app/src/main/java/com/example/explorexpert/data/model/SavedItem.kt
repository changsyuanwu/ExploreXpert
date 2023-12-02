package com.example.explorexpert.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

data class SavedItem(
    var id: String = UUID.randomUUID().toString(),
    val type: SavedItemType = SavedItemType.BLANK,
    val ownerUserId: String = "",
    val title: String = "",
    val description: String = "",
    val placeId: String = "",
    val photoURL: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    var updatedAt: Timestamp? = null,
)
