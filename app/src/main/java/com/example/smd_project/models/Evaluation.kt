package com.example.smd_project.models

data class Evaluation(
    val evaluation_id: Int,
    val course_id: Int,
    val evaluation_type_id: Int,
    val evaluation_number: Int,
    val title: String,
    val total_marks: Int,
    val created_at: String
)

data class CreateEvaluationRequest(
    val courseId: Int,
    val evaluationTypeId: Int,
    val evaluationNumber: Int,
    val title: String,
    val totalMarks: Int,
    val semester: String,
    val academicYear: String
)

data class CreateEvaluationResponse(
    val evaluationId: Int,
    val notificationsCreated: Int
)

data class EvaluationType(
    val evaluation_type_id: Int,
    val evaluation_type_name: String,
    val is_active: Int
)

data class StudentMark(
    val student_id: Int,
    val roll_no: String,
    val full_name: String,
    val obtained_marks: Double?,
    val remarks: String?
)
