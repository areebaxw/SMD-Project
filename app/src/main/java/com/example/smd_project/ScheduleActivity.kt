package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.ScheduleAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class ScheduleActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvSchedule: RecyclerView
    private lateinit var scheduleAdapter: ScheduleAdapter
    
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
        scheduleAdapter = ScheduleAdapter(emptyList())
        rvSchedule.apply {
            layoutManager = LinearLayoutManager(this@ScheduleActivity)
            adapter = scheduleAdapter
        }
    }
    
    private fun loadSchedule() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherSchedule()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val schedules = response.body()?.data ?: emptyList()
                    scheduleAdapter.updateSchedules(schedules)
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
}
