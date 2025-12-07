package com.example.smd_project.models

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val student: Student?,
    val teacher: Teacher?,
    val token: String
)
