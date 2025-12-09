package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.AttendanceSummaryAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentAttendanceActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvAttendance: RecyclerView
    private lateinit var attendanceAdapter: AttendanceSummaryAdapter
    private lateinit var toolbar: Toolbar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_attendance)
        
        sessionManager = SessionManager(this)
        
        // Setup toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Attendance"
        
        // Setup RecyclerView
        rvAttendance = findViewById(R.id.rvAttendance)
        attendanceAdapter = AttendanceSummaryAdapter(emptyList())
        rvAttendance.apply {
            layoutManager = LinearLayoutManager(this@StudentAttendanceActivity)
            adapter = attendanceAdapter
        }
        
        loadAttendanceData()
    }
    
    private fun loadAttendanceData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStudentAttendance()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val attendance = response.body()?.data
                    attendance?.let {
                        if (it.isNotEmpty()) {
                            attendanceAdapter.updateAttendance(it)
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
                Toast.makeText(
                    this@StudentAttendanceActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
