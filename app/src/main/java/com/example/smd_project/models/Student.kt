package com.example.smd_project.models

data class Student(
    val student_id: Int,
    val roll_no: String,
    val full_name: String,
    val email: String,
    val profile_picture_url: String?,
    val phone: String?,
    val date_of_birth: String?,
    val gender: String?,
    val program: String?,
    val batch: String?,
    val semester: Int,
    val cgpa: Double,
    val total_credits: Int,
    val is_active: Boolean,
    val created_at: String
)

data class StudentDashboard(
    val student: StudentInfo,
    val today_classes: List<TodayClass>,
    val announcements: List<Announcement>,
    val attendance_percentage: Double,
    val sgpa: Double,
    val cgpa: Double
)

data class StudentInfo(
    val full_name: String,
    val roll_no: String,
    val cgpa: Double,
    val semester: Int,
    val profile_picture_url: String?
)
