package com.example.signalapp.model

data class FoundUser(
    val userId: String,
    val username: String,
    val displayName: String?,
    val photoUrl: String?
)