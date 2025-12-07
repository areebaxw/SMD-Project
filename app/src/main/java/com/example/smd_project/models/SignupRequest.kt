package com.example.smd_project.models

data class SignupRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String,
    val profileImage: String = ""
)
