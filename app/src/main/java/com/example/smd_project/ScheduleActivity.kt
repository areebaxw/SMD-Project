package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.DayScheduleAdapter
import com.example.smd_project.database.AppDatabase
import com.example.smd_project.models.Schedule
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        lifecycleScope.launch {
            try {
                if (NetworkUtils.isOnline(this@ScheduleActivity)) {
                    // Online: fetch from API and cache
                    val apiService = RetrofitClient.getApiService(sessionManager)
                    val response = apiService.getTeacherSchedule()
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val schedules = response.body()?.data ?: emptyList()
                        
                        // Cache schedules
                        cacheSchedules(schedules)
                        
                        // Group schedules by day of week and sort
                        val schedulesByDay = groupAndSortSchedulesByDay(schedules)
                        dayScheduleAdapter.updateSchedule(schedulesByDay)
                    } else {
                        // Try offline fallback
                        loadOfflineSchedule()
                    }
                } else {
                    // Offline: load from cache
                    loadOfflineSchedule()
                }
            } catch (e: Exception) {
                // On error, try offline fallback
                loadOfflineSchedule()
            }
        }
    }
    
    private suspend fun cacheSchedules(schedules: List<Schedule>) {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(this@ScheduleActivity)
                val teacherId = sessionManager.getUserId()
                
                val entities = schedules.map { schedule ->
                    com.example.smd_project.database.entities.TeacherScheduleEntity(
                        teacher_id = teacherId,
                        course_id = schedule.course_id ?: 0,
                        course_code = schedule.course_code ?: "",
                        course_name = schedule.course_name ?: "",
                        day_of_week = schedule.day_of_week ?: "",
                        start_time = schedule.start_time ?: "",
                        end_time = schedule.end_time ?: "",
                        room = schedule.room_number
                    )
                }
                
                database.teacherScheduleDao().deleteByTeacher(teacherId)
                if (entities.isNotEmpty()) {
                    database.teacherScheduleDao().insertSchedules(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun loadOfflineSchedule() {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(this@ScheduleActivity)
                val teacherId = sessionManager.getUserId()
                val cachedSchedules = database.teacherScheduleDao().getSchedulesSync(teacherId)
                
                if (cachedSchedules.isNotEmpty()) {
                    val schedules = cachedSchedules.map { entity ->
                        Schedule(
                            schedule_id = entity.id,
                            course_id = entity.course_id,
                            course_code = entity.course_code,
                            course_name = entity.course_name,
                            day_of_week = entity.day_of_week,
                            start_time = entity.start_time,
                            end_time = entity.end_time,
                            room_number = entity.room
                        )
                    }
                    
                    withContext(Dispatchers.Main) {
                        val schedulesByDay = groupAndSortSchedulesByDay(schedules)
                        dayScheduleAdapter.updateSchedule(schedulesByDay)
                        Toast.makeText(this@ScheduleActivity, "Showing cached data (offline)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ScheduleActivity, "No cached schedule available", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScheduleActivity, "Error loading offline schedule", Toast.LENGTH_SHORT).show()
                }
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

