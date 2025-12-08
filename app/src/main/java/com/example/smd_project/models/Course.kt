package com.example.smd_project.models

data class Course(
    val course_id: Int,
    val course_code: String,
    val course_name: String,
    val description: String?,
    val credit_hours: Int,
    val semester: Int?,
    val is_required: Boolean,
    val is_active: Int,
    val instructors: String?,
    val schedule: String?,
    val enrolled_students: Int?,
    val grade: String?,
    val gpa: Double?,
    val status: String?
)

data class TodayClass(
    val schedule_id: Int,
    val course_id: Int,
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val room_number: String?,
    val course_name: String,
    val course_code: String,
    val student_count: Int? = null
)
