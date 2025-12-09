package com.example.smd_project

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.FinalGradesAdapter
import com.example.smd_project.models.Course
import com.example.smd_project.models.FinalGradeRecord
import com.example.smd_project.models.StudentFinalGrade
import com.example.smd_project.models.UploadFinalGradesRequest
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadGrade : AppCompatActivity() {

    private lateinit var rvStudentGrades: RecyclerView
    private lateinit var tvCourseName: TextView
    private lateinit var btnSubmitGrades: TextView
    private lateinit var spinnerCourses: Spinner
    private lateinit var btnScaleGrades: TextView
    private val adapter = FinalGradesAdapter()
    private lateinit var sessionManager: SessionManager

    private var courseList: List<Course> = emptyList()
    private var selectedCourseId: Int = 0
    private val academicYear = "2025-2026" // fixed academic year

    // We will fetch ALL types: 1,2,3,4,5
    private val evaluationTypes = listOf(1, 2, 3, 4, 5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_grade)

        sessionManager = SessionManager(this)
        btnScaleGrades = findViewById(R.id.btnScaleGrades)

        btnScaleGrades.setOnClickListener {
            showScaleDialog()
        }

        spinnerCourses = findViewById(R.id.spinnerCourses)
        rvStudentGrades = findViewById(R.id.rvFinalGrades)
        tvCourseName = findViewById(R.id.tvCourseName)
        btnSubmitGrades = findViewById(R.id.btnSubmitGrades)

        rvStudentGrades.layoutManager = LinearLayoutManager(this)
        rvStudentGrades.adapter = adapter

        fetchTeacherCourses()

        btnSubmitGrades.setOnClickListener {
            val gradesList = adapter.currentList.map { student ->
                FinalGradeRecord(
                    studentId = student.studentId,
                    obtainedMarks = student.totalMarks,
                    academicYear = academicYear, // fixed
                    semester = student.semester   // dynamic
                )
            }

            if (gradesList.isEmpty()) {
                Toast.makeText(this, "No grades to submit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use first student's semester as main
            val mainSemester = gradesList.first().semester

            submitFinalGrades(gradesList, academicYear, mainSemester)
        }
    }

    private fun submitFinalGrades(gradesList: List<FinalGradeRecord>, academicYear: String, semester: String) {
        val apiService = RetrofitClient.getApiService(sessionManager)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = UploadFinalGradesRequest(
                    courseId = selectedCourseId,
                    academicYear = academicYear,
                    semester = semester,
                    gradesRecords = gradesList
                )

                val response = apiService.uploadFinalGrades(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@UploadGrade,
                            "Final grades submitted successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val msg = response.body()?.message ?: "Failed to submit grades"
                        Toast.makeText(this@UploadGrade, msg, Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UploadGrade,
                        "Error submitting grades: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showScaleDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_scale_grades, null)
        val etScaleValue = dialogView.findViewById<EditText>(R.id.etScaleValue)
        val btnApply = dialogView.findViewById<TextView>(R.id.btnApplyScale)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnApply.setOnClickListener {
            val scaleAmount = etScaleValue.text.toString().trim().toIntOrNull()
            if (scaleAmount == null) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            applyScalingToAllStudents(scaleAmount)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyScalingToAllStudents(amount: Int) {
        val currentList = adapter.currentList.toMutableList()

        val updatedList = currentList.map { student ->
            val newTotal = student.totalMarks + amount
            student.copy(
                totalMarks = newTotal,
                letterGrade = convertToLetter(newTotal),
                gpa = convertToGPA(newTotal)
            )
        }

        adapter.submitList(updatedList)
        Toast.makeText(this, "Grades scaled by +$amount", Toast.LENGTH_SHORT).show()
    }

    private fun fetchTeacherCourses() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getTeacherCourses()
                if (response.isSuccessful && response.body()?.success == true) {
                    courseList = response.body()!!.data ?: emptyList()
                    if (courseList.isNotEmpty()) {
                        val courseNames = courseList.map { it.course_name }
                        withContext(Dispatchers.Main) {
                            val spinnerAdapter = ArrayAdapter(
                                this@UploadGrade,
                                android.R.layout.simple_spinner_item,
                                courseNames
                            )
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinnerCourses.adapter = spinnerAdapter

                            selectedCourseId = courseList.first().course_id
                            tvCourseName.text = courseList.first().course_name
                            fetchAllEvaluations(selectedCourseId)
                        }

                        spinnerCourses.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val course = courseList[position]
                                selectedCourseId = course.course_id
                                tvCourseName.text = course.course_name
                                fetchAllEvaluations(selectedCourseId)
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@UploadGrade, "No courses found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UploadGrade, "Error fetching courses", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchAllEvaluations(courseId: Int) {
        val apiService = RetrofitClient.getApiService(sessionManager)

        CoroutineScope(Dispatchers.IO).launch {
            val allStudentGrades = mutableMapOf<Int, StudentFinalGrade>()

            for (typeId in evaluationTypes) {
                try {
                    val response = apiService.getEvaluationMarks(courseId, typeId)
                    if (response.isSuccessful && response.body()?.data != null) {
                        val data = response.body()!!.data
                        val studentsData = data?.students ?: emptyList()
                        val evaluations = data?.evaluations ?: emptyList()

                        studentsData.forEach { student ->
                            val totalForThisType = evaluations
                                .flatMap { it.marks }
                                .filter { it.student_id == student.student_id }
                                .sumOf { it.obtained_marks ?: 0.0 }

                            val existing = allStudentGrades[student.student_id]

                            if (existing == null) {
                                allStudentGrades[student.student_id] = StudentFinalGrade(
                                    studentId = student.student_id,
                                    fullName = student.full_name,
                                    profilePic = student.profile_picture_url,
                                    totalMarks = totalForThisType,
                                    letterGrade = "",
                                    gpa = 0.0,
                                    academicYear = "2025-2026",
                                    semester = student.semester.toString()
                                )
                            } else {
                                existing.totalMarks += totalForThisType
                            }
                        }
                    }
                } catch (e: Exception) { /* continue */ }
            }

            val finalList = allStudentGrades.values.map {
                it.copy(
                    letterGrade = convertToLetter(it.totalMarks),
                    gpa = convertToGPA(it.totalMarks)
                )
            }

            withContext(Dispatchers.Main) {
                adapter.submitList(finalList)
            }
        }
    }

    private fun convertToLetter(marks: Double) = when {
        marks >= 90 -> "A+"
        marks >= 85 -> "A"
        marks >= 80 -> "A-"
        marks >= 75 -> "B+"
        marks >= 70 -> "B"
        marks >= 65 -> "B-"
        marks >= 60 -> "C+"
        marks >= 55 -> "C"
        marks >= 50 -> "C-"
        else -> "F"
    }

    private fun convertToGPA(marks: Double) = when {
        marks >= 90 -> 4.0
        marks >= 85 -> 3.7
        marks >= 80 -> 3.5
        marks >= 75 -> 3.0
        marks >= 70 -> 2.7
        marks >= 65 -> 2.3
        marks >= 60 -> 2.0
        marks >= 55 -> 1.7
        marks >= 50 -> 1.0
        else -> 0.0
    }
}
