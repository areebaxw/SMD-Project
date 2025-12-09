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

// Models for evaluation marks response
data class EvaluationMarksResponse(
    val students: List<Student>,
    val evaluations: List<EvaluationWithMarks>,
    val evaluation_type_id: Int
)

data class EvaluationWithMarks(
    val evaluation_id: Int,
    val evaluation_number: Int,
    val title: String,
    val total_marks: Int,
    val academic_year: String,
    val semester: String,
    val marks: List<StudentMark>,
    val weightage: String? = "-",
    val stats: EvaluationStats? = null
)

data class EvaluationStats(
    val highest: Double?,
    val lowest: Double?,
    val average: Double?,
    val total_marked: Int?
)

data class StudentMark(
    val student_id: Int,
    val obtained_marks: Double?,
    val marked_at: String?,
    val roll_no: String = "",
    val full_name: String = "",
    val remarks: String? = null
)

// Models for final grade submission
data class FinalGradeRecord(
    val studentId: Int,
    val obtainedMarks: Double,
    val academicYear: String,
    val semester: String
)

data class UploadFinalGradesRequest(
    val courseId: Int,
    val academicYear: String,
    val semester: String,
    val gradesRecords: List<FinalGradeRecord>
)
