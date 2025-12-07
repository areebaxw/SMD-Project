package com.example.smd_project.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: Map<String, String>? = null
)
