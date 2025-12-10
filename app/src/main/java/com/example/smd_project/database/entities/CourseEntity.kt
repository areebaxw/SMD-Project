package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val course_id: Int,
    val course_code: String,
    val course_name: String,
    val description: String?,
    val credit_hours: Int,
    val semester: Int?,
    val is_required: Int,
    val is_active: Int,
    val created_at: String,
    val updated_at: String,
    val last_synced_at: Long = System.currentTimeMillis()
)
