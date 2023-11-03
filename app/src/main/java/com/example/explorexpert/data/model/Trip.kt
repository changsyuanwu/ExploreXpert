package com.example.explorexpert.data.model

import java.util.UUID

data class Trip(
    var id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val isPrivate: Boolean = true,
    val ownerUserId: String = "",
    val sharedUsers: MutableList<SharedUserRecord> = mutableListOf(),
    val savedItemIds: MutableList<String> = mutableListOf(),
)
