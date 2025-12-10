package com.example.smd_project.models

data class StudentFinalGrade(
    val studentId: Int,
    val fullName: String,
    val profilePic: String?,
    var totalMarks: Double,
    val letterGrade: String,
    val academicYear: String,
    val semester: String,
    val gpa: Double
)
data class CGPARequest(
    val cgpa: Double,
    val total_credits: Int
)
