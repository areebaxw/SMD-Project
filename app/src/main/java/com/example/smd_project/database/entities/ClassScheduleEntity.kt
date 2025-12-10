package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_schedule")
data class ClassScheduleEntity(
    @PrimaryKey
    val schedule_id: Int,
    val course_id: Int,
    val teacher_id: Int,
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val room_number: String?,
    val academic_year: String,
    val semester: String,
    val is_active: Int,
    val created_at: String,
    val course_name: String? = null,
    val course_code: String? = null,
    val last_synced_at: Long = System.currentTimeMillis()
)
