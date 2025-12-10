package com.example.smd_project.models

data class PayFeeRequest(
    val student_id: Int,
    val fee_id: Int,
    val amount: Double,
    val method: String,
    val remarks: String? = null
)
