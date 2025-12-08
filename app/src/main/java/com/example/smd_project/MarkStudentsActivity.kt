package com.example.smd_project

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.StudentMarkAdapter
import com.example.smd_project.models.EnterMarksRequest
import com.example.smd_project.models.MarksRecordItem
import com.example.smd_project.models.StudentMark
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class MarkStudentsActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private var evaluationId: Int = 0
    private var courseId: Int = 0
    private var evaluationTitle: String = ""
    private var totalMarks: Int = 0
    
    private lateinit var tvEvaluationTitle: TextView
    private lateinit var tvTotalMarks: TextView
    private lateinit var rvStudents: RecyclerView
    private lateinit var btnSubmitMarks: Button
    
    private lateinit var studentMarkAdapter: StudentMarkAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mark_students)
        
        sessionManager = SessionManager(this)
        
        evaluationId = intent.getIntExtra("evaluation_id", 0)
        courseId = intent.getIntExtra("course_id", 0)
        evaluationTitle = intent.getStringExtra("evaluation_title") ?: ""
        totalMarks = intent.getIntExtra("total_marks", 0)
        
        if (evaluationId == 0 || courseId == 0) {
            Toast.makeText(this, "Invalid evaluation or course", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadStudents()
    }
    
    private fun initViews() {
        tvEvaluationTitle = findViewById(R.id.tvEvaluationTitle)
        tvTotalMarks = findViewById(R.id.tvTotalMarks)
        rvStudents = findViewById(R.id.rvStudents)
        btnSubmitMarks = findViewById(R.id.btnSubmitMarks)
        
        tvEvaluationTitle.text = evaluationTitle
        tvTotalMarks.text = "Total Marks: $totalMarks"
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        studentMarkAdapter = StudentMarkAdapter(emptyList(), totalMarks)
        rvStudents.apply {
            layoutManager = LinearLayoutManager(this@MarkStudentsActivity)
            adapter = studentMarkAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnSubmitMarks.setOnClickListener {
            submitMarks()
        }
    }
    
    private fun loadStudents() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getCourseStudents(courseId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val students = response.body()?.data ?: emptyList()
                    val studentMarks = students.map { student ->
                        StudentMark(
                            student_id = student.student_id,
                            roll_no = student.roll_no,
                            full_name = student.full_name,
                            obtained_marks = null,
                            remarks = null
                        )
                    }
                    studentMarkAdapter.updateStudents(studentMarks)
                } else {
                    Toast.makeText(
                        this@MarkStudentsActivity,
                        "Failed to load students",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MarkStudentsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun submitMarks() {
        val marksData = studentMarkAdapter.getMarksData()
        
        if (marksData.isEmpty()) {
            Toast.makeText(this, "No marks to submit", Toast.LENGTH_SHORT).show()
            return
        }
        
        val request = EnterMarksRequest(
            course_id = courseId,
            evaluation_type_id = 1, // Will be updated by backend if needed
            evaluation_number = 1,
            title = evaluationTitle,
            total_marks = totalMarks,
            academic_year = getCurrentAcademicYear(),
            semester = getCurrentSemester(),
            marks_records = marksData
        )
        
        submitMarksToServer(request)
    }
    
    private fun submitMarksToServer(request: EnterMarksRequest) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                btnSubmitMarks.isEnabled = false
                val response = apiService.markStudentAssessment(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(
                        this@MarkStudentsActivity,
                        "Marks submitted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                        this@MarkStudentsActivity,
                        response.body()?.message ?: "Failed to submit marks",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnSubmitMarks.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MarkStudentsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                btnSubmitMarks.isEnabled = true
            }
        }
    }
    
    private fun getCurrentAcademicYear(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        return if (calendar.get(java.util.Calendar.MONTH) >= 7) {
            "$year-${year + 1}"
        } else {
            "${year - 1}-$year"
        }
    }
    
    private fun getCurrentSemester(): String {
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.get(java.util.Calendar.MONTH)
        return if (month >= 7 || month < 3) "Fall" else "Spring"
    }
}
