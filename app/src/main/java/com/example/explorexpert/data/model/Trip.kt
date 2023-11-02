package com.example.explorexpert.data.model

data class Trip(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val isPrivate: Boolean = true,
    val ownerUserId: String = "",
    val sharedUsers: MutableList<SharedUserRecord> = mutableListOf(),
    val savedItemIds: MutableList<String> = mutableListOf(),
)
