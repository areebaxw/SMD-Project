package com.example.smd_project.models

import com.google.gson.annotations.SerializedName

data class AttendanceSummary(
    val student_id: Int,
    val full_name: String,
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
    @SerializedName("courseId")
    val courseId: Int,
    @SerializedName("attendanceRecords")
    val attendanceRecords: List<AttendanceItem>,
    @SerializedName("attendanceDate")
    val attendanceDate: String? = null  // Optional: defaults to today if not provided
)

data class AttendanceItem(
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("status")
    val status: String
)

// Response model for today's attendance (uses snake_case from backend)
data class TodayAttendanceItem(
    val student_id: Int,
    val status: String
)

data class MarkAttendanceResponse(
    val marked: Int,
    val notified: Int
)
