package com.example.smd_project.models

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("fullName")
    val fullName: String,
    val email: String,
    val password: String,
    val role: String,
    @SerializedName("profileImage")
    val profileImage: String = "",
    val gender: String,
    val program: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    val phone: String? = null,
    val department: String? = null,
    val designation: String? = null,
    val specialization: String? = null
)
