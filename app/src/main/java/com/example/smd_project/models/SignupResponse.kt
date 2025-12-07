package com.example.smd_project.models

data class SignupResponse(
    val success: Boolean,
    val message: String,
    val data: SignupData?
)

data class SignupData(
    val rollNumber: String,
    val fullName: String,
    val email: String,
    val role: String,
    val profileImage: String
)
