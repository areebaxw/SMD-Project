package com.example.smd_project.models

data class Notification(
    val notification_id: Int,
    val recipient_type: String, // "Student", "Teacher", "All"
    val recipient_id: Int?,
    val title: String,
    val message: String,
    val notification_type: String, // "Announcement", "Attendance", "Marks", "Fee", "General"
    val reference_id: Int?,
    val is_read: Boolean,
    val created_at: String
)

data class NotificationResponse(
    val total: Int,
    val unread: Int,
    val notifications: List<Notification>
)
