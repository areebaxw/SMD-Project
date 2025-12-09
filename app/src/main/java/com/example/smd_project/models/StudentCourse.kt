package com.example.smd_project.models

data class StudentCourse(
    val course_id: Int,
    val course_code: String,
    val course_name: String,
    val description: String?,
    val credit_hours: Int,
    val semester: Int?,
    val is_required: Int,
    val is_active: Int,
    val created_at: String,
    val updated_at: String,
    val grade: String?,
    val gpa: Double?,
    val status: String,
    val teacher_name: String,
    val teacher_id: Int
)

data class CourseMarksDetail(
    val mark_id: Int,
    val evaluation_id: Int,
    val student_id: Int,
    val obtained_marks: Double,
    val remarks: String?,
    val marked_by: Int,
    val marked_at: String,
    val updated_at: String,
    val title: String,
    val total_marks: Int,
    val type_name: String,
    val course_name: String,
    val course_code: String,
    val percentage: Double
)

data class CourseEvaluation(
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
    val type_name: String,
    val course_name: String,
    val course_code: String,
    val obtained_marks: Double?,
    val teacher_name: String
)

data class StudentFee(
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
    val program: String,
    val semester: Int,
    val academic_year: String,
    val amount_paid_total: Double
)

data class StudentGPA(
    val cgpa: Double,
    val sgpa: Double,
    val total_credits: Int
)
