package com.example.smd_project.models

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("fullName")
    val fullName: String,
    val email: String,
    val password: String,
    val role: String,
    @SerializedName("profileImage")
    val profileImage: String = ""
)
