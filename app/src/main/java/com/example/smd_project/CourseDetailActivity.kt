package com.example.smd_project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.StudentAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class CourseDetailActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private var courseId: Int = 0
    private var courseName: String = ""
    private var courseCode: String = ""
    
    private lateinit var tvCourseName: TextView
    private lateinit var tvCourseCode: TextView
    private lateinit var tvStudentCount: TextView
    private lateinit var rvStudents: RecyclerView
    private lateinit var btnMarkAttendance: Button
    private lateinit var btnViewEvaluations: Button
    
    private lateinit var studentAdapter: StudentAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)
        
        sessionManager = SessionManager(this)
        
        // Get course data from intent
        courseId = intent.getIntExtra("course_id", 0)
        courseName = intent.getStringExtra("course_name") ?: ""
        courseCode = intent.getStringExtra("course_code") ?: ""
        
        if (courseId == 0) {
            Toast.makeText(this, "Invalid course", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initViews()
        setupRecyclerView()
        loadCourseStudents()
    }
    
    private fun initViews() {
        tvCourseName = findViewById(R.id.tvCourseName)
        tvCourseCode = findViewById(R.id.tvCourseCode)
        tvStudentCount = findViewById(R.id.tvStudentCount)
        rvStudents = findViewById(R.id.rvStudents)


        tvCourseName.text = courseName
        tvCourseCode.text = courseCode
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter(emptyList())
        rvStudents.apply {
            layoutManager = LinearLayoutManager(this@CourseDetailActivity)
            adapter = studentAdapter
        }
    }
    

    private fun loadCourseStudents() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getCourseStudents(courseId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val students = response.body()?.data ?: emptyList()
                    studentAdapter.updateStudents(students)
                    tvStudentCount.text = "Total Students: ${students.size}"
                } else {
                    Toast.makeText(
                        this@CourseDetailActivity,
                        "Failed to load students",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CourseDetailActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
