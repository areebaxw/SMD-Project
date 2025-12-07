package com.example.smd_project.models

data class Teacher(
    val teacher_id: Int,
    val employee_id: String,
    val full_name: String,
    val email: String,
    val profile_picture_url: String?,
    val phone: String?,
    val department: String?,
    val designation: String?,
    val specialization: String?,
    val is_active: Boolean,
    val created_at: String
)

data class TeacherDashboard(
    val teacher: TeacherInfo,
    val course_count: Int,
    val student_count: Int,
    val today_classes: List<TodayClass>,
    val recent_activity: List<Announcement>,
    val pending_tasks: Int
)

data class TeacherInfo(
    val full_name: String,
    val employee_id: String,
    val department: String?,
    val profile_picture_url: String?
)
