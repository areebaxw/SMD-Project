package com.example.smd_project.models

data class FeeDetails(
    val fees: List<Fee>,
    val payment_history: List<Payment>
)

data class Fee(
    val fee_id: Int,
    val total_amount: Double,
    val paid_amount: Double,
    val remaining_amount: Double,
    val payment_status: String,
    val due_date: String?,
    val program: String,
    val semester: Int,
    val academic_year: String
)

data class Payment(
    val payment_id: Int,
    val amount_paid: Double,
    val payment_date: String,
    val payment_method: String,
    val transaction_id: String?,
    val remarks: String?
)
