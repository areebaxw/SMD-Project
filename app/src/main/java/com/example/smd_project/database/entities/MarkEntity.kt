package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marks")
data class MarkEntity(
    @PrimaryKey
    val mark_id: Int,
    val evaluation_id: Int,
    val student_id: Int,
    val obtained_marks: Double,
    val remarks: String?,
    val marked_by: Int,
    val marked_at: String,
    val updated_at: String,
    val title: String? = null,
    val total_marks: Int? = null,
    val evaluation_number: Int? = null,
    val type_name: String? = null,
    val course_name: String? = null,
    val course_code: String? = null,
    val percentage: Double? = null,
    val last_synced_at: Long = System.currentTimeMillis()
)
