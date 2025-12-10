package com.example.smd_project.models

data class Semester(
    val id: Int,
    val name: String
)

data class TranscriptCourse(
    val course_name: String,
    val course_code: String,
    val credit_hours: Int,
    val marks: Int,
    val grade: String,
    val grade_points: Float
)

data class TranscriptResponse(
    val student_id: Int,
    val total_credits: Int,
    val sgpa: Float,
    val cgpa: Float,
    val courses: List<TranscriptCourse>
)
