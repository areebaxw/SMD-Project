package com.example.smd_project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.StudentAdapter
import com.example.smd_project.models.AttendanceRecordItem
import com.example.smd_project.models.MarkAttendanceRequest
import com.example.smd_project.models.Student
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MarkAttendance : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var etDate: EditText
    private lateinit var spinnerCourse: Spinner
    private lateinit var rvStudents: RecyclerView
    private lateinit var btnSubmit: Button
    private lateinit var btnAllPresent: Button
    
    private lateinit var studentAdapter: StudentAdapter
    private val attendanceMap = mutableMapOf<Int, String>() // student_id to status
    private var selectedCourseId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markattendance)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerView()
        loadCourses()
        setupClickListeners()
        
        // Set today's date
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(today)
    }
    
    private fun initViews() {
        etDate = findViewById(R.id.etDate)
        spinnerCourse = findViewById(R.id.spinnerCourse)
        rvStudents = findViewById(R.id.rvStudents)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnAllPresent = findViewById(R.id.btnAllPresent)
    }
    
    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter(emptyList(), showCheckbox = true) { student, isChecked ->
            attendanceMap[student.student_id] = if (isChecked) "Present" else "Absent"
        }
        
        rvStudents.apply {
            layoutManager = LinearLayoutManager(this@MarkAttendance)
            adapter = studentAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnAllPresent.setOnClickListener {
            // Mark all as present
            val apiService = RetrofitClient.getApiService(sessionManager)
            lifecycleScope.launch {
                try {
                    val response = apiService.getCourseStudents(selectedCourseId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.forEach { student ->
                            attendanceMap[student.student_id] = "Present"
                        }
                        Toast.makeText(this@MarkAttendance, "All marked as present", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        btnSubmit.setOnClickListener {
            submitAttendance()
        }
    }
    
    private fun loadCourses() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherCourses()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val courses = response.body()?.data
                    
                    // You'll need to populate spinner with courses
                    // For now, just load students for first course if available
                    courses?.firstOrNull()?.let { course ->
                        selectedCourseId = course.course_id
                        loadStudents(course.course_id)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MarkAttendance, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun loadStudents(courseId: Int) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getCourseStudents(courseId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val students = response.body()?.data ?: emptyList()
                    studentAdapter.updateStudents(students)
                    
                    // Initialize attendance map with all absent
                    students.forEach { student ->
                        attendanceMap[student.student_id] = "Absent"
                    }
                } else {
                    Toast.makeText(this@MarkAttendance,
                        "Failed to load students",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MarkAttendance, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun submitAttendance() {
        val date = etDate.text.toString()
        
        if (selectedCourseId == 0) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show()
            return
        }
        
        val records = attendanceMap.map { (studentId, status) ->
            AttendanceRecordItem(studentId, status)
        }
        
        val request = MarkAttendanceRequest(
            course_id = selectedCourseId,
            attendance_date = date,
            attendance_records = records
        )
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.markAttendance(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MarkAttendance,
                        "Attendance marked successfully",
                        Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@MarkAttendance,
                        response.body()?.message ?: "Failed to mark attendance",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MarkAttendance, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
