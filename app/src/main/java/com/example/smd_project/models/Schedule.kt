package com.example.smd_project.models

data class Schedule(
    val schedule_id: Int,
    val course_id: Int,
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val room_number: String?,
    val course_name: String,
    val course_code: String
)

data class ClassScheduleRequest(
    val course_id: Int,
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val room_number: String?,
    val academic_year: String,
    val semester: String
)
