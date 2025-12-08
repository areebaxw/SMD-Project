package com.example.smd_project.models

data class Evaluation(
    val evaluation_id: Int,
    val course_id: Int,
    val course_name: String,
    val course_code: String,
    val teacher_id: Int,
    val evaluation_type_id: Int,
    val evaluation_type: String,
    val evaluation_number: Int,
    val title: String,
    val description: String?,
    val total_marks: Int,
    val due_date: String?,
    val academic_year: String,
    val semester: String,
    val created_at: String,
    val updated_at: String
)

data class CreateEvaluationRequest(
    val course_id: Int,
    val evaluation_type_id: Int,
    val evaluation_number: Int,
    val title: String,
    val description: String?,
    val total_marks: Int,
    val due_date: String?,
    val academic_year: String,
    val semester: String
)

data class EvaluationType(
    val evaluation_type_id: Int,
    val type_name: String,
    val weightage_percentage: Double
)

data class StudentMark(
    val student_id: Int,
    val roll_no: String,
    val full_name: String,
    val obtained_marks: Double?,
    val remarks: String?
)
