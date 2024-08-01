package com.example.voiceassistant // Replace with your package name

import java.util.UUID

data class User(
    val id: UUID? = null, // Use UUID for the id field
    val username: String,
    val password: String
)
