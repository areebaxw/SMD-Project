package com.example.smd_project.models

data class PayFeeRequest(
    val student_id: Int,
    val fee_id: Int,
    val amount: Double,
    val method: String,
    val remarks: String? = null
)
data class PaymentHistoryItem(
    val payment_id: Int,
    val student_id: Int,
    val fee_id: Int,
    val amount_paid: Double,
    val method: String,
    val remarks: String?,
    val created_at: String
)
