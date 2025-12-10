package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey
    val attendance_id: Int,
    val student_id: Int,
    val course_id: Int,
    val teacher_id: Int,
    val attendance_date: String,
    val status: String,
    val remarks: String?,
    val marked_at: String,
    val course_name: String? = null,
    val course_code: String? = null,
    val last_synced_at: Long = System.currentTimeMillis()
)
