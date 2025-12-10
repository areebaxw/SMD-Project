package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val student_id: Int,
    val roll_no: String,
    val full_name: String,
    val email: String,
    val profile_picture_url: String?,
    val phone: String?,
    val date_of_birth: String?,
    val gender: String?,
    val program: String?,
    val batch: String?,
    val semester: Int?,
    val cgpa: Double?,
    val total_credits: Int?,
    val is_active: Int,
    val created_at: String,
    val updated_at: String,
    val last_synced_at: Long = System.currentTimeMillis()
)
