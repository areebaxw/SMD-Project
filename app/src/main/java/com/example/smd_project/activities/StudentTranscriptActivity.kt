package com.example.smd_project.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.adapters.TranscriptCourseAdapter
import com.example.smd_project.models.CGPARequest
import com.example.smd_project.models.TranscriptCourse
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentTranscriptActivity : AppCompatActivity() {

    private lateinit var spinnerSemesters: Spinner
    private lateinit var rvCourses: RecyclerView
    private lateinit var tvTotalCredits: TextView
    private lateinit var tvSGPA: TextView
    private lateinit var tvCGPA: TextView
    private lateinit var tvNoCourses: TextView

    private lateinit var adapter: TranscriptCourseAdapter
    private val semesters = mutableListOf<String>()
    private var transcriptData: Map<String, Map<String, Any>> = emptyMap()

    private val TAG = "StudentTranscriptActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_transcript)

        spinnerSemesters = findViewById(R.id.spinner_semesters)
        rvCourses = findViewById(R.id.rv_transcript_courses)
        tvTotalCredits = findViewById(R.id.tv_total_credits_value)
        tvSGPA = findViewById(R.id.tv_total_semesters)
        tvCGPA = findViewById(R.id.tv_cgpa)
        tvNoCourses = findViewById(R.id.tv_no_courses)

        // Setup back button
        val backButton: android.widget.ImageView? = findViewById(R.id.back_button)
        backButton?.apply {
            setColorFilter(android.graphics.Color.WHITE)
            setOnClickListener { finish() }
        }

        adapter = TranscriptCourseAdapter(emptyList())
        rvCourses.layoutManager = LinearLayoutManager(this)
        rvCourses.adapter = adapter

        loadTranscript()
    }

    private fun loadTranscript() {
        val apiService = RetrofitClient.getApiService(SessionManager(this))
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching transcript from server...")
                val response = apiService.getStudentTranscript()
                val data = response.body()?.data

                Log.d(TAG, "Transcript response code: ${response.code()}")
                Log.d(TAG, "Transcript response body: ${response.body()}")

                if (response.isSuccessful && data != null) {

                    // Parse transcript safely
                    @Suppress("UNCHECKED_CAST")
                    transcriptData =
                        (data["transcript"] as? Map<String, Any>)?.mapNotNull { (semester, value) ->
                            val semesterMap = value as? Map<String, Any> ?: return@mapNotNull null
                            semester to semesterMap
                        }?.toMap() ?: emptyMap()

                    // Populate dropdown
                    semesters.clear()
                    semesters.addAll(transcriptData.keys.sorted())
                    setupSemesterSpinner()

                    // Calculate CGPA
                    val (cgpa, totalCredits) = calculateCGPA(data)
                    tvCGPA.text = String.format("%.2f", cgpa)

                    Log.d(TAG, "Calculated CGPA: $cgpa, Total Credits: $totalCredits")

                    // Save to database
                    saveCGPAToDatabase(cgpa, totalCredits)
                } else {
                    Log.e(TAG, "Failed to fetch transcript: ${response.code()} ${response.errorBody()}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching transcript", e)
            }
        }
    }

    private fun setupSemesterSpinner() {
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSemesters.adapter = adapterSpinner

        spinnerSemesters.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val semesterName = semesters[position]
                displaySemesterTranscript(semesterName)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun displaySemesterTranscript(semesterName: String) {
        val semesterData = transcriptData[semesterName] as? Map<*, *>
        val coursesList = (semesterData?.get("courses") as? List<Map<String, Any>>) ?: emptyList()

        if (coursesList.isEmpty()) {
            rvCourses.visibility = View.GONE
            tvNoCourses.visibility = View.VISIBLE
            tvTotalCredits.text = "0"
            tvSGPA.text = "0.00"
            return
        } else {
            rvCourses.visibility = View.VISIBLE
            tvNoCourses.visibility = View.GONE
        }

        val courses = coursesList.map { courseMap ->
            val gradePoints = when (val gp = courseMap["grade_points"]) {
                is Double -> gp.toFloat()
                is String -> gp.toFloatOrNull() ?: 0f
                else -> 0f
            }

            TranscriptCourse(
                course_name = courseMap["course_name"] as? String ?: "",
                course_code = courseMap["course_code"] as? String ?: "",
                credit_hours = (courseMap["credit_hours"] as? Double)?.toInt() ?: 0,
                marks = 0,
                grade = courseMap["grade"] as? String ?: "",
                grade_points = gradePoints
            )
        }

        adapter.updateCourses(courses)

        val totalCredits = courses.sumOf { it.credit_hours }
        tvTotalCredits.text = totalCredits.toString()

        val coursesWithGrades = courses.filter { it.grade_points > 0 }
        val sgpa = if (coursesWithGrades.isNotEmpty()) {
            coursesWithGrades.sumOf { it.credit_hours * it.grade_points.toDouble() } /
                    coursesWithGrades.sumOf { it.credit_hours }
        } else null

        tvSGPA.text = if (sgpa == null) {
            "—"
        } else {
            String.format("%.2f", sgpa)
        }

    }

    private fun calculateCGPA(apiData: Map<String, Any>): Pair<Double, Int> {
        val transcript = apiData["transcript"] as? Map<String, Any> ?: emptyMap()

        val coursesWithGrades = transcript.values.flatMap { semesterAny ->
            val semesterMap = semesterAny as? Map<*, *> ?: return@flatMap emptyList<TranscriptCourse>()
            val coursesList = semesterMap["courses"] as? List<Map<String, Any>> ?: emptyList()

            coursesList.mapNotNull { courseMap ->
                val gradePoints = when (val gp = courseMap["grade_points"]) {
                    is String -> gp.toFloatOrNull()
                    is Double -> gp.toFloat()
                    else -> null
                }
                val creditHours = (courseMap["credit_hours"] as? Double)?.toInt() ?: 0
                if (gradePoints != null && creditHours > 0) {
                    TranscriptCourse(
                        course_name = courseMap["course_name"] as? String ?: "",
                        course_code = courseMap["course_code"] as? String ?: "",
                        credit_hours = creditHours,
                        marks = 0,
                        grade = courseMap["grade"] as? String ?: "",
                        grade_points = gradePoints
                    )
                } else null
            }
        }

        val totalCredits = coursesWithGrades.sumOf { it.credit_hours }

        val cgpa = if (totalCredits > 0) {
            coursesWithGrades.sumOf { it.credit_hours * it.grade_points.toDouble() } / totalCredits
        } else 0.0

        return Pair(cgpa, totalCredits)
    }

    private fun saveCGPAToDatabase(cgpa: Double, totalCredits: Int) {
        val apiService = RetrofitClient.getApiService(SessionManager(this))
        lifecycleScope.launch {
            try {
                val requestBody = CGPARequest(cgpa, totalCredits)
                Log.d(TAG, "Sending CGPA update: $requestBody")

                val response = apiService.updateCGPA(requestBody)

                Log.d(TAG, "CGPA update response code: ${response.code()}")
                Log.d(TAG, "CGPA update response body: ${response.body()}")

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@StudentTranscriptActivity,
                        "CGPA saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(TAG, "CGPA update failed: ${response.errorBody()}")
                    Toast.makeText(
                        this@StudentTranscriptActivity,
                        "Failed to save CGPA",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while saving CGPA", e)
            }
        }
    }

}
