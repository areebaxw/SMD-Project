package com.example.smd_project.models

data class ImageUploadResponse(
    val success: Boolean,
    val imageUrl: String?,
    val message: String?
)
