package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.CourseAdapter
import com.example.smd_project.models.Course
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class CourseListActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvCourses: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerView()
        loadCourses()
    }
    
    private fun initViews() {
        rvCourses = findViewById(R.id.rvCourses)
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(emptyList()) { course ->
            navigateToCourseDetail(course)
        }
        rvCourses.apply {
            layoutManager = LinearLayoutManager(this@CourseListActivity)
            adapter = courseAdapter
        }
    }
    
    private fun loadCourses() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherCourses()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val courses = response.body()?.data ?: emptyList()
                    courseAdapter.updateCourses(courses)
                } else {
                    Toast.makeText(
                        this@CourseListActivity,
                        "Failed to load courses",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CourseListActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun navigateToCourseDetail(course: Course) {
        val intent = Intent(this, CourseDetailActivity::class.java).apply {
            putExtra("course_id", course.course_id)
            putExtra("course_name", course.course_name)
            putExtra("course_code", course.course_code)
        }
        startActivity(intent)
    }
}
