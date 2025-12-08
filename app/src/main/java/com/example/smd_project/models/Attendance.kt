package com.example.smd_project.models

data class AttendanceSummary(
    val student_id: Int,
    val first_name: String,
    val last_name: String,
    val present_count: Int,
    val absent_count: Int,
    val late_count: Int,
    val total_classes: Int,
    val attendance_percentage: Double
)

data class AttendanceRecord(
    val attendance_date: String,
    val status: String,
    val remarks: String?
)

data class MarkAttendanceRequest(
    val courseId: Int,
    val attendance: List<AttendanceItem>
)

data class AttendanceItem(
    val studentId: Int,
    val status: String
)

data class MarkAttendanceResponse(
    val marked: Int,
    val notified: Int
)
