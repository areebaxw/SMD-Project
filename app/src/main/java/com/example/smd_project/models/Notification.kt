package com.example.smd_project.models

data class Notification(
    val notification_id: Int,
    val recipient_type: String,
    val recipient_id: Int,
    val title: String,
    val message: String,
    val notification_type: String,
    val is_read: Int,
    val created_at: String
)

data class NotificationResponse(
    val total: Int,
    val unread: Int,
    val notifications: List<Notification>
)

data class UnreadCountResponse(
    val unreadCount: Int
)
