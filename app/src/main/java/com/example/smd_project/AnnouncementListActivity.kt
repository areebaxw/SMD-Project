package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.AnnouncementAdapter
import com.example.smd_project.models.Announcement
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.repository.StudentRepository
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class AnnouncementListActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: StudentRepository
    private lateinit var rvAnnouncements: RecyclerView
    private lateinit var announcementAdapter: AnnouncementAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement_list)
        
        sessionManager = SessionManager(this)
        repository = StudentRepository(this)
        
        initViews()
        setupRecyclerView()
        loadAnnouncements()
    }
    
    private fun initViews() {
        rvAnnouncements = findViewById(R.id.rvAnnouncements)
        
        // Setup toolbar with white back arrow
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar?.apply {
            setNavigationOnClickListener { finish() }
            // Make back arrow white
            val backArrow = navigationIcon
            if (backArrow != null) {
                backArrow.setTint(android.graphics.Color.WHITE)
                navigationIcon = backArrow
            }
        }
    }
    
    private fun setupRecyclerView() {
        announcementAdapter = AnnouncementAdapter(emptyList())
        rvAnnouncements.apply {
            layoutManager = LinearLayoutManager(this@AnnouncementListActivity)
            adapter = announcementAdapter
        }
    }
    
    private fun loadAnnouncements() {
        val userType = sessionManager.getUserType()
        
        if (userType == "Student") {
            // Use repository for student announcements (with offline support via LiveData)
            repository.getAnnouncements().observe(this) { announcementEntities ->
                val announcements = announcementEntities.map { entity ->
                    Announcement(
                        announcement_id = entity.announcement_id,
                        teacher_id = entity.teacher_id,
                        course_id = entity.course_id,
                        title = entity.title,
                        content = entity.content,
                        announcement_type = entity.announcement_type,
                        is_active = entity.is_active,
                        created_at = entity.created_at,
                        teacher_name = entity.teacher_name,
                        course_name = entity.course_name,
                        course_code = null,
                        updated_at = entity.updated_at
                    )
                }
                if (announcements.isNotEmpty()) {
                    announcementAdapter.updateAnnouncements(announcements)
                } else {
                    Toast.makeText(
                        this@AnnouncementListActivity,
                        "No announcements available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            // Trigger refresh from network if online
            lifecycleScope.launch {
                repository.refreshAnnouncements()
            }
        } else {
            // Teacher announcements - check network first
            lifecycleScope.launch {
                try {
                    if (!NetworkUtils.isOnline(this@AnnouncementListActivity)) {
                        Toast.makeText(
                            this@AnnouncementListActivity,
                            "No internet connection. Please try again when online.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                    
                    val apiService = RetrofitClient.getApiService(sessionManager)
                    val response = apiService.getTeacherAnnouncements()
                    if (response.isSuccessful && response.body()?.success == true) {
                        val announcements = response.body()?.data ?: emptyList()
                        if (announcements.isNotEmpty()) {
                            announcementAdapter.updateAnnouncements(announcements)
                        } else {
                            Toast.makeText(
                                this@AnnouncementListActivity,
                                "No announcements available",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@AnnouncementListActivity,
                            "Failed to load announcements",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@AnnouncementListActivity,
                        "Error loading announcements: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
