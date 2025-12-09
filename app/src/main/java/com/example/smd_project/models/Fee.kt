package com.example.smd_project.models

data class FeeDetails(
    val fees: List<Fee>,
    val payment_history: List<Payment>
)

data class Fee(
    val fee_id: Int,
    val total_amount: Double,
    var paid_amount: Double,
    var remaining_amount: Double,
    var payment_status: String,
    val due_date: String?,
    val program: String,
    val semester: Int,
    val academic_year: String,
    val student_id: Int
)

data class Payment(
    val payment_id: Int,
    val amount_paid: Double,
    val payment_date: String,
    val payment_method: String,
    val transaction_id: String?,
    val remarks: String?
)
// Each fee item
data class StudentFeeItem(
    val fee_id: Int,
    val student_id: Int,
    val fee_structure_id: Int,
    val program: String,
    val semester: Int,
    val academic_year: String,
    val total_amount: Double,
    val paid_amount: Double?,
    val remaining_amount: Double,
    val payment_status: String,
    val due_date: String?
)


// Data wrapper
data class StudentFeesData(
    val fees: List<StudentFeeItem>
)

// API response wrapper
data class StudentFeesResponse(
    val success: Boolean,
    val message: String?,
    val data: List<StudentFeeItem>?
)
data class UpdateTotalFeeRequest(
    val student_id: Int,
    val total_amount: Double
)

// Response can be generic, just success message
data class UpdateTotalFeeResponse(
    val success: Boolean,
    val message: String
)