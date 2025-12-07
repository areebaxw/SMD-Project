package com.example.smd_project.models

data class RollNumberResponse(
    val success: Boolean,
    val lastRollNumber: String?,
    val nextRollNumber: String
)
