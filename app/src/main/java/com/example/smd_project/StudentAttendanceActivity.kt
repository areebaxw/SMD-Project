package com.example.smd_project

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.AttendanceRecordAdapter
import com.example.smd_project.models.AttendanceSummary
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentAttendanceActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvAttendance: RecyclerView
    private lateinit var attendanceAdapter: AttendanceRecordAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var spinnerCourse: Spinner
    private lateinit var tvOverallPercentage: android.widget.TextView
    
    private var attendanceSummaryList: List<AttendanceSummary> = emptyList()
    private var selectedCourseId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_attendance)
        
        sessionManager = SessionManager(this)
        
        // Setup toolbar with white back arrow
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Attendance"
        // Make back arrow white
        val backArrow = toolbar.navigationIcon
        if (backArrow != null) {
            backArrow.setTint(android.graphics.Color.WHITE)
            toolbar.navigationIcon = backArrow
        }
        
        // Setup overall percentage view
        tvOverallPercentage = findViewById(R.id.tvOverallPercentage)
        
        // Setup course spinner
        spinnerCourse = findViewById(R.id.spinnerCourse)
        
        // Setup RecyclerView
        rvAttendance = findViewById(R.id.rvAttendance)
        attendanceAdapter = AttendanceRecordAdapter(mutableListOf())
        rvAttendance.apply {
            layoutManager = LinearLayoutManager(this@StudentAttendanceActivity)
            adapter = attendanceAdapter
        }
        
        loadAttendanceData()
    }
    
    private fun loadAttendanceData() {
        // Check network before making API call
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "No internet connection. Attendance requires online access.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("StudentAttendance", "Loading attendance data...")
                val response = apiService.getStudentAttendance()
                android.util.Log.d("StudentAttendance", "Response received: ${response.code()}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val attendance = response.body()?.data
                    android.util.Log.d("StudentAttendance", "Data size: ${attendance?.size}")
                    
                    attendance?.let {
                        if (it.isNotEmpty()) {
                            attendanceSummaryList = it
                            setupCourseSpinner(it)
                            // Load first course's records by default
                            selectedCourseId = it[0].course_id
                            loadCourseRecords(selectedCourseId)
                        } else {
                            Toast.makeText(
                                this@StudentAttendanceActivity,
                                "No attendance records available",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@StudentAttendanceActivity,
                        response.body()?.message ?: "Failed to load attendance",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("StudentAttendance", "Exception: ${e.message}", e)
                Toast.makeText(
                    this@StudentAttendanceActivity,
                    "Failed to load attendance. Please check your connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun setupCourseSpinner(courses: List<AttendanceSummary>) {
        val courseNames = courses.map { "${it.course_name} (${it.course_code})" }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            courseNames
        )
        spinnerCourse.adapter = adapter
        
        spinnerCourse.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position >= 0 && position < courses.size) {
                    selectedCourseId = courses[position].course_id
                    loadCourseRecords(selectedCourseId)
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun loadCourseRecords(courseId: Int) {
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        // Get the selected course to display its overall attendance
        val selectedCourse = attendanceSummaryList.find { it.course_id == courseId }
        if (selectedCourse != null) {
            tvOverallPercentage.text = String.format("%.1f%%", selectedCourse.percentage)
        }
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("StudentAttendance", "Loading records for course: $courseId")
                val response = apiService.getAttendanceDetails(courseId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val records = response.body()?.data
                    android.util.Log.d("StudentAttendance", "Records size: ${records?.size}")
                    
                    records?.let {
                        if (it.isNotEmpty()) {
                            // Sort by date in descending order (newest first)
                            val sortedRecords = it.sortedByDescending { record -> record.attendance_date }
                            attendanceAdapter.updateRecords(sortedRecords)
                        } else {
                            attendanceAdapter.updateRecords(emptyList())
                            Toast.makeText(
                                this@StudentAttendanceActivity,
                                "No records found for this course",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@StudentAttendanceActivity,
                        response.body()?.message ?: "Failed to load records",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("StudentAttendance", "Exception loading records: ${e.message}", e)
                Toast.makeText(
                    this@StudentAttendanceActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

