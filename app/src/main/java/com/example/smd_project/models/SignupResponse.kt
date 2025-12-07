package com.example.smd_project.models

import com.google.gson.annotations.SerializedName

data class SignupResponse(
    val success: Boolean,
    val message: String,
    val data: SignupData?
)

data class SignupData(
    @SerializedName("rollNumber")
    val rollNumber: String,
    @SerializedName("fullName")
    val fullName: String,
    val email: String,
    val role: String,
    @SerializedName("profileImage")
    val profileImage: String
)
