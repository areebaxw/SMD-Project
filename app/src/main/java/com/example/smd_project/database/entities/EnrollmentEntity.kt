package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "enrollments")
data class EnrollmentEntity(
    @PrimaryKey
    val enrollment_id: Int,
    val student_id: Int,
    val course_id: Int,
    val academic_year: String,
    val semester: String,
    val enrollment_date: String,
    val status: String,
    val grade: String?,
    val gpa: Double?,
    val created_at: String,
    val updated_at: String,
    val course_code: String? = null,
    val course_name: String? = null,
    val description: String? = null,
    val credit_hours: Int? = null,
    val teacher_name: String? = null,
    val teacher_id: Int? = null,
    val last_synced_at: Long = System.currentTimeMillis()
)
