package com.example.explorexpert.data.model

data class SavedItem(
    val id: String = "",
    val type: SavedItemType = SavedItemType.BLANK,
    val data: String = "",
)
