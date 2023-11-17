package com.example.explorexpert.data.model

import java.util.UUID

data class SavedItem(
    val id: String = UUID.randomUUID().toString(),
    val type: SavedItemType = SavedItemType.BLANK,
    val imgURL: String = "",
    val title: String = "",
    val description: String = "",
)
