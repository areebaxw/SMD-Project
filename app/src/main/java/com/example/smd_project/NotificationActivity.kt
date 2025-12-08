package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.NotificationAdapter
import com.example.smd_project.models.Notification
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvNotifications: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerView()
        loadNotifications()
    }
    
    private fun initViews() {
        rvNotifications = findViewById(R.id.rvNotifications)
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(emptyList()) { notification ->
            onNotificationClicked(notification)
        }
        rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = notificationAdapter
        }
    }
    
    private fun loadNotifications() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherNotifications()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val notificationResponse = response.body()?.data
                    notificationResponse?.let {
                        notificationAdapter.updateNotifications(it.notifications)
                        title = "Notifications (${it.unread} unread)"
                    }
                } else {
                    Toast.makeText(
                        this@NotificationActivity,
                        "Failed to load notifications",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@NotificationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun onNotificationClicked(notification: Notification) {
        if (!notification.is_read) {
            markNotificationAsRead(notification.notification_id)
        }
    }
    
    private fun markNotificationAsRead(notificationId: Int) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.markNotificationAsRead(notificationId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Reload notifications
                    loadNotifications()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
