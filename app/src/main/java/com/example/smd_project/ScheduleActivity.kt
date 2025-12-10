package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.DayScheduleAdapter
import com.example.smd_project.models.Schedule
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class ScheduleActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvSchedule: RecyclerView
    private lateinit var dayScheduleAdapter: DayScheduleAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerView()
        loadSchedule()
    }
    
    private fun initViews() {
        rvSchedule = findViewById(R.id.rvSchedule)
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        dayScheduleAdapter = DayScheduleAdapter(emptyMap())
        rvSchedule.apply {
            layoutManager = LinearLayoutManager(this@ScheduleActivity)
            adapter = dayScheduleAdapter
        }
    }
    
    private fun loadSchedule() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherSchedule()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val schedules = response.body()?.data ?: emptyList()
                    
                    // Group schedules by day of week and sort
                    val schedulesByDay = groupAndSortSchedulesByDay(schedules)
                    
                    dayScheduleAdapter.updateSchedule(schedulesByDay)
                } else {
                    Toast.makeText(
                        this@ScheduleActivity,
                        "Failed to load schedule",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ScheduleActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun groupAndSortSchedulesByDay(schedules: List<Schedule>): Map<String, List<Schedule>> {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        // Group schedules by day of week
        val groupedByDay = schedules.groupBy { it.day_of_week }
        
        // Create a map with all days in order, sorting classes by start time within each day
        val result = mutableMapOf<String, List<Schedule>>()
        for (day in days) {
            val classesForDay = groupedByDay[day]?.sortedBy { it.start_time } ?: emptyList()
            result[day] = classesForDay
        }
        
        return result
    }
}

