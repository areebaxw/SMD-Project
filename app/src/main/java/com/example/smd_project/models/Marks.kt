package com.example.smd_project.models

data class Mark(
    val course_name: String,
    val course_code: String,
    val evaluation_type: String,
    val evaluation_number: Int,
    val title: String,
    val total_marks: Int,
    val obtained_marks: Double,
    val percentage: Double
)

data class MarksResponse(
    val marks: List<Mark>,
    val grouped_marks: Map<String, Map<String, List<Mark>>>
)

data class EnterMarksRequest(
    val course_id: Int,
    val evaluation_type_id: Int,
    val evaluation_number: Int,
    val title: String,
    val total_marks: Int,
    val academic_year: String,
    val semester: String,
    val marks_records: List<MarksRecordItem>
)

data class MarksRecordItem(
    val student_id: Int,
    val obtained_marks: Double
)

data class MarkStudentAssessmentRequest(
    val evaluationId: Int,
    val studentId: Int,
    val obtainedMarks: Int
)

data class MarkStudentAssessmentResponse(
    val marksId: Int,
    val studentId: Int,
    val obtainedMarks: Int,
    val totalMarks: Int
)

data class CourseMarks(
    val evaluation_id: Int,
    val student_id: Int,
    val full_name: String,
    val obtained_marks: Int,
    val evaluation_number: Int,
    val title: String,
    val total_marks: Int
)
