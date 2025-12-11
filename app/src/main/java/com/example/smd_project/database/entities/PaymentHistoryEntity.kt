package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey
    val payment_id: Int,
    val student_id: Int,
    val fee_id: Int,
    val amount_paid: Double,
    val payment_method: String,
    val remarks: String?,
    val created_at: String,
    val last_synced_at: Long = System.currentTimeMillis()
)
