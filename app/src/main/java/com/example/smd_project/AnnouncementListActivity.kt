package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.AnnouncementAdapter
import com.example.smd_project.database.AppDatabase
import com.example.smd_project.models.Announcement
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.repository.StudentRepository
import com.example.smd_project.repository.TeacherRepository
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnnouncementListActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var studentRepository: StudentRepository
    private lateinit var teacherRepository: TeacherRepository
    private lateinit var rvAnnouncements: RecyclerView
    private lateinit var announcementAdapter: AnnouncementAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement_list)
        
        sessionManager = SessionManager(this)
        studentRepository = StudentRepository(this)
        teacherRepository = TeacherRepository(this)
        
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
            studentRepository.getAnnouncements().observe(this) { announcementEntities ->
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
                studentRepository.refreshAnnouncements()
            }
        } else {
            // Teacher announcements with offline support
            loadTeacherAnnouncements()
        }
    }
    
    private fun loadTeacherAnnouncements() {
        lifecycleScope.launch {
            try {
                val isOnline = NetworkUtils.isOnline(this@AnnouncementListActivity)
                val result = teacherRepository.getAnnouncements(forceRefresh = isOnline)
                
                result.onSuccess { announcements ->
                    if (announcements.isNotEmpty()) {
                        announcementAdapter.updateAnnouncements(announcements)
                        // Also cache to Room for offline access
                        if (isOnline) {
                            cacheTeacherAnnouncements(announcements)
                        }
                        if (!isOnline) {
                            Toast.makeText(
                                this@AnnouncementListActivity,
                                "Showing cached data (offline)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Try loading from Room cache
                        loadOfflineTeacherAnnouncements()
                    }
                }.onFailure {
                    // Try loading from Room cache on failure
                    loadOfflineTeacherAnnouncements()
                }
            } catch (e: Exception) {
                loadOfflineTeacherAnnouncements()
            }
        }
    }
    
    private suspend fun cacheTeacherAnnouncements(announcements: List<Announcement>) {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(this@AnnouncementListActivity)
                val teacherId = sessionManager.getUserId()
                
                val entities = announcements.map { announcement ->
                    com.example.smd_project.database.entities.TeacherAnnouncementEntity(
                        announcement_id = announcement.announcement_id,
                        teacher_id = teacherId,
                        course_id = announcement.course_id,
                        title = announcement.title ?: "",
                        content = announcement.content ?: "",
                        created_at = announcement.created_at ?: "",
                        is_synced = true
                    )
                }
                
                database.teacherAnnouncementDao().deleteByTeacher(teacherId)
                if (entities.isNotEmpty()) {
                    database.teacherAnnouncementDao().insertAnnouncements(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun loadOfflineTeacherAnnouncements() {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(this@AnnouncementListActivity)
                val teacherId = sessionManager.getUserId()
                val cachedAnnouncements = database.teacherAnnouncementDao().getAnnouncementsSync(teacherId)
                
                if (cachedAnnouncements.isNotEmpty()) {
                    val announcements = cachedAnnouncements.map { entity ->
                        Announcement(
                            announcement_id = entity.announcement_id,
                            teacher_id = entity.teacher_id,
                            course_id = entity.course_id,
                            title = entity.title,
                            content = entity.content,
                            announcement_type = "General",
                            is_active = 1,
                            created_at = entity.created_at,
                            teacher_name = null,
                            course_name = null,
                            course_code = null,
                            updated_at = null
                        )
                    }
                    
                    withContext(Dispatchers.Main) {
                        announcementAdapter.updateAnnouncements(announcements)
                        Toast.makeText(this@AnnouncementListActivity, "Showing cached data (offline)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AnnouncementListActivity, "No cached announcements available", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AnnouncementListActivity, "Error loading offline data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
