package com.example.smd_project.models

import com.google.gson.annotations.SerializedName

data class Course(
    val course_id: Int,
    val course_code: String,
    val course_name: String,
    val description: String?,
    val credit_hours: Int,
    val semester: Int?,
    val is_required: Int,  // Changed from Boolean to Int (0 or 1 from TINYINT)
    val is_active: Int,
    val instructors: String?,
    val schedule: String?,
    val enrolled_students: Int?,
    val grade: String?,
    val gpa: Double?,
    val status: String?
)
data class CourseDetails(
    @SerializedName("course_id") val course_id: Int,
    @SerializedName("course_code") val course_code: String,
    @SerializedName("course_name") val course_name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("credit_hours") val credit_hours: Int,
    @SerializedName("semester") val semester: Int,
    @SerializedName("is_required") val isRequired: Int,
    @SerializedName("is_active") val isActive: Int,
    val instructors: List<instructors>
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
data class CourseResponse(
    val success: Boolean,
    val message: String,
    val data: CourseDetails,
)
data class instructors(
    val teacherId: Int,
    val full_name: String
)