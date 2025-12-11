package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing attendance summary per course for offline access.
 * This stores the aggregated attendance data (present/absent/late counts) per course.
 */
@Entity(tableName = "attendance_summary")
data class AttendanceSummaryEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int, // Computed as: course_id * 10000 + student_id
    val student_id: Int,
    val course_id: Int,
    val course_name: String,
    val course_code: String,
    val present_count: Int,
    val absent_count: Int,
    val late_count: Int,
    val excused_count: Int,
    val total_count: Int,
    val percentage: Double,
    val last_synced_at: Long = System.currentTimeMillis()
)
