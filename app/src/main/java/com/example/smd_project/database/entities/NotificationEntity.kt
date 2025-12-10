package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val notification_id: Int,
    val recipient_type: String,
    val recipient_id: Int?,
    val title: String,
    val message: String,
    val notification_type: String,
    val reference_id: Int?,
    val is_read: Int,
    val created_at: String,
    val last_synced_at: Long = System.currentTimeMillis()
)
