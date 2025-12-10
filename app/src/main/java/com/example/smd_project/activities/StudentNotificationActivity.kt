package com.example.smd_project.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.smd_project.R
import com.example.smd_project.adapters.StudentNotificationAdapter
import com.example.smd_project.models.Notification
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.repository.StudentRepository
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentNotificationActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var repository: StudentRepository
    private lateinit var rvNotifications: RecyclerView
    private lateinit var notificationAdapter: StudentNotificationAdapter
    private lateinit var backButton: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvNoNotifications: View
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvUnreadCount: TextView

    private var notifications: MutableList<Notification> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_notification)

        sessionManager = SessionManager(this)
        repository = StudentRepository(this)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        observeNotifications()
        observeUnreadCount()
    }

    private fun initViews() {
        rvNotifications = findViewById(R.id.rvNotifications)
        backButton = findViewById(R.id.backButton)
        tvTitle = findViewById(R.id.tvTitle)
        tvNoNotifications = findViewById(R.id.tvNoNotifications)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        tvUnreadCount = findViewById(R.id.tvUnreadCount)

        tvTitle.text = "Notifications"
    }

    private fun setupRecyclerView() {
        notificationAdapter = StudentNotificationAdapter(
            notifications = notifications,
            onNotificationClick = { notification ->
                markNotificationAsRead(notification)
            },
            onDeleteClick = { notification ->
                // Optional: Implement delete functionality
                Toast.makeText(this, "Delete functionality coming soon", Toast.LENGTH_SHORT).show()
            }
        )

        rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@StudentNotificationActivity)
            adapter = notificationAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        swipeRefresh.setOnRefreshListener {
            refreshNotifications()
        }
    }

    private fun observeNotifications() {
        repository.getNotifications(100).observe(this) { notificationEntities ->
            val notificationList = notificationEntities.map { entity ->
                Notification(
                    notification_id = entity.notification_id,
                    recipient_type = entity.recipient_type,
                    recipient_id = entity.recipient_id ?: 0,
                    title = entity.title,
                    message = entity.message,
                    notification_type = entity.notification_type,
                    is_read = entity.is_read,
                    created_at = entity.created_at
                )
            }
            
            notifications.clear()
            notifications.addAll(notificationList)
            notificationAdapter.notifyDataSetChanged()
            
            // Show/hide empty state
            if (notifications.isEmpty()) {
                tvNoNotifications.visibility = View.VISIBLE
                rvNotifications.visibility = View.GONE
            } else {
                tvNoNotifications.visibility = View.GONE
                rvNotifications.visibility = View.VISIBLE
            }
            
            showLoading(false)
        }
        
        // Initial refresh
        refreshNotifications()
    }
    
    private fun observeUnreadCount() {
        repository.getUnreadNotificationCount().observe(this) { unreadCount ->
            updateUnreadCount(unreadCount)
        }
    }
    
    private fun refreshNotifications() {
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            swipeRefresh.isRefreshing = false
            return
        }
        
        lifecycleScope.launch {
            swipeRefresh.isRefreshing = true
            repository.refreshNotifications()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun markNotificationAsRead(notification: Notification) {
        if (notification.is_read == 1) return // Already read

        lifecycleScope.launch {
            try {
                repository.markNotificationAsRead(notification.notification_id)
                // LiveData will automatically update the UI
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUnreadCount(count: Int) {
        if (count > 0) {
            tvUnreadCount.visibility = View.VISIBLE
            tvUnreadCount.text = if (count > 99) "99+" else count.toString()
        } else {
            tvUnreadCount.visibility = View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        swipeRefresh.isRefreshing = false
        if (show) {
            progressBar.visibility = View.VISIBLE
            rvNotifications.visibility = View.GONE
            tvNoNotifications.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh notifications when returning to this activity
        if (NetworkUtils.isOnline(this)) {
            lifecycleScope.launch {
                repository.refreshNotifications()
            }
        }
    }
}
