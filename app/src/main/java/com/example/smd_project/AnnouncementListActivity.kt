package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.AnnouncementAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class AnnouncementListActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvAnnouncements: RecyclerView
    private lateinit var announcementAdapter: AnnouncementAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement_list)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerView()
        loadAnnouncements()
    }
    
    private fun initViews() {
        rvAnnouncements = findViewById(R.id.rvAnnouncements)
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
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
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherAnnouncements()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val announcements = response.body()?.data ?: emptyList()
                    announcementAdapter.updateAnnouncements(announcements)
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
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
