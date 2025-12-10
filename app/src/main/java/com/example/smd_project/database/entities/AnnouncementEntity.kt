package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey
    val announcement_id: Int,
    val teacher_id: Int,
    val course_id: Int?,
    val title: String,
    val content: String,
    val announcement_type: String,
    val is_active: Int,
    val created_at: String,
    val updated_at: String,
    val teacher_name: String? = null,
    val course_name: String? = null,
    val last_synced_at: Long = System.currentTimeMillis()
)
