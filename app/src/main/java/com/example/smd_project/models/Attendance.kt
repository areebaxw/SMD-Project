package com.example.smd_project.models

data class AttendanceSummary(
    val course_name: String,
    val course_code: String,
    val total_classes: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val percentage: Double
)

data class AttendanceRecord(
    val attendance_date: String,
    val status: String,
    val remarks: String?
)

data class MarkAttendanceRequest(
    val course_id: Int,
    val attendance_date: String,
    val attendance_records: List<AttendanceRecordItem>
)

data class AttendanceRecordItem(
    val student_id: Int,
    val status: String
)
