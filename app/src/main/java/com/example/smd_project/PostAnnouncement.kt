package com.example.smd_project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.models.PostAnnouncementRequest
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class PostAnnouncement : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var spinnerCourse: Spinner
    private lateinit var spinnerType: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnPublish: Button
    
    private var selectedCourseId: Int? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postannouncement)
        
        sessionManager = SessionManager(this)
        
        initViews()
        loadCourses()
        setupClickListeners()
    }
    
    private fun initViews() {
        spinnerCourse = findViewById(R.id.spinnerCourse)
        spinnerType = findViewById(R.id.spinnerType)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnPublish = findViewById(R.id.btnPublish)
    }
    
    private fun setupClickListeners() {
        btnPublish.setOnClickListener {
            publishAnnouncement()
        }
    }
    
    private fun loadCourses() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherCourses()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val courses = response.body()?.data
                    // Populate spinner with courses
                    courses?.firstOrNull()?.let { course ->
                        selectedCourseId = course.course_id
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@PostAnnouncement, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun publishAnnouncement() {
        val title = etTitle.text.toString()
        val content = etContent.text.toString()
        
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val announcementType = "General" // Get from spinner
        
        val request = PostAnnouncementRequest(
            course_id = selectedCourseId,
            title = title,
            content = content,
            announcement_type = announcementType
        )
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.postAnnouncement(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@PostAnnouncement,
                        "Announcement posted successfully",
                        Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@PostAnnouncement,
                        response.body()?.message ?: "Failed to post announcement",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PostAnnouncement, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
