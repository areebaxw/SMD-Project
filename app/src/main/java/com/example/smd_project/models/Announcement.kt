package com.example.smd_project.models

data class Announcement(
    val announcement_id: Int,
    val teacher_id: Int,
    val course_id: Int?,
    val title: String,
    val content: String,
    val announcement_type: String,
    val is_active: Int,
    val created_at: String,
    val teacher_name: String,
    val course_name: String?,
    val course_code: String?
)

data class PostAnnouncementRequest(
    val course_id: Int?,
    val title: String,
    val content: String,
    val announcement_type: String
)
