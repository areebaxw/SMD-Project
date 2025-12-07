package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.TodayClassAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class TeacherDashboard : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvTeacherName: TextView
    private lateinit var tvEmployeeId: TextView
    private lateinit var tvCourseCount: TextView
    private lateinit var tvStudentCount: TextView
    private lateinit var rvTodayClasses: RecyclerView
    
    private lateinit var todayClassAdapter: TodayClassAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacherdashboard)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerViews()
        setupClickListeners()
        loadDashboardData()
    }
    
    private fun initViews() {
        try {
            ivProfilePic = findViewById(R.id.ivProfilePic)
            tvTeacherName = findViewById(R.id.tvTeacherName)
            tvEmployeeId = findViewById(R.id.tvEmployeeId)
            tvCourseCount = findViewById(R.id.tvCourseCount)
            tvStudentCount = findViewById(R.id.tvStudentCount)
            rvTodayClasses = findViewById(R.id.rvTodayClasses)
            
            tvTeacherName.text = sessionManager.getUserName()
            
            sessionManager.getProfilePic()?.let { url ->
                Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivProfilePic)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupRecyclerViews() {
        todayClassAdapter = TodayClassAdapter(emptyList())
        rvTodayClasses.apply {
            layoutManager = LinearLayoutManager(this@TeacherDashboard)
            adapter = todayClassAdapter
        }
    }
    
    private fun setupClickListeners() {
        try {
            findViewById<View>(R.id.btnMarkAttendance)?.setOnClickListener {
                startActivity(Intent(this, MarkAttendance::class.java))
            }
            
            findViewById<View>(R.id.btnEnterMarks)?.setOnClickListener {
                startActivity(Intent(this, EnterMarks::class.java))
            }
            
            findViewById<View>(R.id.btnPostAnnouncement)?.setOnClickListener {
                startActivity(Intent(this, PostAnnouncement::class.java))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadDashboardData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherDashboard()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val dashboard = response.body()?.data
                    
                    dashboard?.let {
                        tvTeacherName.text = it.teacher.full_name
                        tvEmployeeId.text = it.teacher.employee_id
                        tvCourseCount.text = it.course_count.toString()
                        tvStudentCount.text = it.student_count.toString()
                        
                        it.teacher.profile_picture_url?.let { url ->
                            Picasso.get()
                                .load(url)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(ivProfilePic)
                        }
                        
                        todayClassAdapter.updateClasses(it.today_classes)
                    }
                } else {
                    Toast.makeText(this@TeacherDashboard,
                        response.body()?.message ?: "Failed to load dashboard",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TeacherDashboard,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
