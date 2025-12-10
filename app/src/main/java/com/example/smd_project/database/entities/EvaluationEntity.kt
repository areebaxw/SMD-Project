package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evaluations")
data class EvaluationEntity(
    @PrimaryKey
    val evaluation_id: Int,
    val course_id: Int,
    val teacher_id: Int,
    val evaluation_type_id: Int,
    val evaluation_number: Int,
    val title: String,
    val description: String?,
    val total_marks: Int,
    val due_date: String?,
    val academic_year: String,
    val semester: String,
    val created_at: String,
    val updated_at: String,
    val weightage: Double?,
    val type_name: String? = null,
    val course_name: String? = null,
    val course_code: String? = null,
    val teacher_name: String? = null,
    val obtained_marks: Double? = null,
    val last_synced_at: Long = System.currentTimeMillis()
)
