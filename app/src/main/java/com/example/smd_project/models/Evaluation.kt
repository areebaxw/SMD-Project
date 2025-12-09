package com.example.smd_project.models

import com.google.gson.annotations.SerializedName

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
    val weightage: String? = null
)

data class CreateEvaluationResponse(
    val evaluationId: Int,
    val notificationsCreated: Int
)

data class EvaluationType(
    val evaluation_type_id: Int,
    @SerializedName("type_name")
    val evaluation_type_name: String,
    val evaluation_type: String = "",
    val is_active: Int = 1,
    val weightage_percentage: Int = 0
)
