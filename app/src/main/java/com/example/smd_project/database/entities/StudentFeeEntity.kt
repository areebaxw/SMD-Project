package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_fees")
data class StudentFeeEntity(
    @PrimaryKey
    val fee_id: Int,
    val student_id: Int,
    val fee_structure_id: Int,
    val total_amount: Double,
    val paid_amount: Double,
    val remaining_amount: Double,
    val payment_status: String,
    val due_date: String?,
    val created_at: String,
    val updated_at: String,
    val program: String? = null,
    val semester: Int? = null,
    val academic_year: String? = null,
    val structure_total_fee: Double? = null,
    val last_synced_at: Long = System.currentTimeMillis()
)
