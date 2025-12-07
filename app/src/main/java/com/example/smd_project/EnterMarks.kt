package com.example.smd_project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.models.EnterMarksRequest
import com.example.smd_project.models.MarksRecordItem
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class EnterMarks : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var spinnerCourse: Spinner
    private lateinit var spinnerEvaluationType: Spinner
    private lateinit var etEvaluationNumber: EditText
    private lateinit var etTitle: EditText
    private lateinit var etTotalMarks: EditText
    private lateinit var btnSubmit: Button
    
    private var selectedCourseId: Int = 0
    private var selectedEvaluationTypeId: Int = 1 // Default to Assignment
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entermarks)
        
        sessionManager = SessionManager(this)
        
        initViews()
        loadCourses()
        setupClickListeners()
    }
    
    private fun initViews() {
        spinnerCourse = findViewById(R.id.spinnerCourse)
        spinnerEvaluationType = findViewById(R.id.spinnerEvaluationType)
        etEvaluationNumber = findViewById(R.id.etEvaluationNumber)
        etTitle = findViewById(R.id.etTitle)
        etTotalMarks = findViewById(R.id.etTotalMarks)
        btnSubmit = findViewById(R.id.btnSubmit)
    }
    
    private fun setupClickListeners() {
        btnSubmit.setOnClickListener {
            submitMarks()
        }
    }
    
    private fun loadCourses() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherCourses()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val courses = response.body()?.data
                    courses?.firstOrNull()?.let { course ->
                        selectedCourseId = course.course_id
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnterMarks, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun submitMarks() {
        val evaluationNumber = etEvaluationNumber.text.toString().toIntOrNull() ?: 0
        val title = etTitle.text.toString()
        val totalMarks = etTotalMarks.text.toString().toIntOrNull() ?: 0
        
        if (selectedCourseId == 0 || evaluationNumber == 0 || title.isEmpty() || totalMarks == 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        // For demo, create dummy marks records
        val marksRecords = listOf(
            MarksRecordItem(1, 85.0),
            MarksRecordItem(2, 90.0)
        )
        
        val request = EnterMarksRequest(
            course_id = selectedCourseId,
            evaluation_type_id = selectedEvaluationTypeId,
            evaluation_number = evaluationNumber,
            title = title,
            total_marks = totalMarks,
            academic_year = "2025",
            semester = "Fall",
            marks_records = marksRecords
        )
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.enterMarks(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EnterMarks, "Marks entered successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EnterMarks,
                        response.body()?.message ?: "Failed to enter marks",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnterMarks, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
