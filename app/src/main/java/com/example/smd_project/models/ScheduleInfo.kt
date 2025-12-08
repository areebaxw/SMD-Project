package com.example.smd_project.models

data class ScheduleInfo(
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val room_number: String?
)