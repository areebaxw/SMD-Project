package com.example.smd_project

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.DayScheduleAdapter
import com.example.smd_project.models.Schedule
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentScheduleActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvSchedule: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var dayScheduleAdapter: DayScheduleAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_schedule)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerView()
        loadSchedule()
    }
    
    private fun initViews() {
        rvSchedule = findViewById(R.id.rvSchedule)
        backButton = findViewById(R.id.backButton)
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        dayScheduleAdapter = DayScheduleAdapter(emptyMap())
        rvSchedule.apply {
            layoutManager = LinearLayoutManager(this@StudentScheduleActivity)
            adapter = dayScheduleAdapter
        }
    }
    
    private fun loadSchedule() {
        // Check network before making API call
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "No internet connection. Schedule requires online access.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStudentSchedule()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val schedules = response.body()?.data ?: emptyList()
                    
                    // Group schedules by day of week
                    val groupedSchedules = groupSchedulesByDay(schedules)
                    dayScheduleAdapter.updateSchedule(groupedSchedules)
                } else {
                    Toast.makeText(
                        this@StudentScheduleActivity,
                        "Failed to load schedule",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@StudentScheduleActivity,
                    "Failed to load schedule. Please check your connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun groupSchedulesByDay(schedules: List<Schedule>): Map<String, List<Schedule>> {
        val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val grouped = schedules.groupBy { it.day_of_week }
        
        // Return in order of days
        return daysOrder.associateWith { day ->
            grouped[day] ?: emptyList()
        }
    }
}
