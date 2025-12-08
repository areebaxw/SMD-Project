package com.example.smd_project.models

data class CourseRegistrationRequest(
    val courseIds: List<Int>,
    val academicYear: String,
    val semester: String
)