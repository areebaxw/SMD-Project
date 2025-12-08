package com.example.smd_project.models

import com.google.gson.annotations.SerializedName

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
    val is_active: Int,
    val created_at: String
)

data class TeacherDashboard(
    val teacher: TeacherInfo,
    val courses: List<Course>?,
    val todaySchedule: List<TodayClass>?,
    val unreadNotifications: Int,
    val stats: DashboardStats? = null
)

data class TeacherInfo(
    val teacher_id: Int,
    val full_name: String,
    val email: String,
    val phone: String?,
    @SerializedName("profile_image")
    val profile_picture_url: String?
)
