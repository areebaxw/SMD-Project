package com.example.smd_project.models

import com.google.gson.annotations.SerializedName

data class ImageUploadResponse(
    val success: Boolean,
    val message: String?,
    val data: ImageUploadData?
)

data class ImageUploadData(
    @SerializedName("url")
    val url: String?,
    @SerializedName("publicId")
    val publicId: String?
)
