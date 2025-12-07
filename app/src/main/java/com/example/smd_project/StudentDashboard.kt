package com.example.smd_project

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.AnnouncementAdapter
import com.example.smd_project.adapters.TodayClassAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class StudentDashboard : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvStudentName: TextView
    private lateinit var tvRollNo: TextView
    private lateinit var tvSGPA: TextView
    private lateinit var tvCGPA: TextView
    private lateinit var tvAttendancePercentage: TextView
    private lateinit var rvTodayClasses: RecyclerView
    private lateinit var rvAnnouncements: RecyclerView
    
    private lateinit var todayClassAdapter: TodayClassAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studentdashboard)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerViews()
        loadDashboardData()
    }
    
    private fun initViews() {
        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvRollNo = findViewById(R.id.tvRollNo)
        tvSGPA = findViewById(R.id.tvSGPA)
        tvCGPA = findViewById(R.id.tvCGPA)
        tvAttendancePercentage = findViewById(R.id.tvAttendancePercentage)
        rvTodayClasses = findViewById(R.id.rvTodayClasses)
        rvAnnouncements = findViewById(R.id.rvAnnouncements)
        
        // Load saved profile pic if available
        sessionManager.getProfilePic()?.let { url ->
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(ivProfilePic)
        }
        
        tvStudentName.text = sessionManager.getUserName()
    }
    
    private fun setupRecyclerViews() {
        // Today's Classes RecyclerView
        todayClassAdapter = TodayClassAdapter(emptyList())
        rvTodayClasses.apply {
            layoutManager = LinearLayoutManager(this@StudentDashboard)
            adapter = todayClassAdapter
        }
        
        // Announcements RecyclerView
        announcementAdapter = AnnouncementAdapter(emptyList())
        rvAnnouncements.apply {
            layoutManager = LinearLayoutManager(this@StudentDashboard)
            adapter = announcementAdapter
        }
    }
    
    private fun loadDashboardData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStudentDashboard()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val dashboard = response.body()?.data
                    
                    dashboard?.let {
                        // Update UI with dashboard data
                        tvStudentName.text = it.student.full_name
                        tvRollNo.text = it.student.roll_no
                        tvSGPA.text = String.format("%.2f", it.sgpa)
                        tvCGPA.text = String.format("%.2f", it.cgpa)
                        tvAttendancePercentage.text = "${it.attendance_percentage}%"
                        
                        // Load profile picture
                        it.student.profile_picture_url?.let { url ->
                            Picasso.get()
                                .load(url)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(ivProfilePic)
                        }
                        
                        // Update RecyclerViews
                        todayClassAdapter.updateClasses(it.today_classes)
                        announcementAdapter.updateAnnouncements(it.announcements)
                    }
                } else {
                    Toast.makeText(this@StudentDashboard, 
                        response.body()?.message ?: "Failed to load dashboard", 
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentDashboard, 
                    "Error: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
