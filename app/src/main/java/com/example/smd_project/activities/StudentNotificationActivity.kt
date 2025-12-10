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
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentNotificationActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
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

        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadNotifications()
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
            loadNotifications()
        }
    }

    private fun loadNotifications() {
        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            try {
                showLoading(true)

                val response = apiService.getStudentNotifications(limit = 100)

                if (response.isSuccessful && response.body()?.success == true) {
                    val notificationList = response.body()?.data ?: emptyList()

                    notifications.clear()
                    notifications.addAll(notificationList)

                    notificationAdapter.notifyDataSetChanged()

                    // Update unread count
                    val unreadCount = notifications.count { it.is_read == 0 }
                    updateUnreadCount(unreadCount)

                    // Show/hide empty state
                    if (notifications.isEmpty()) {
                        tvNoNotifications.visibility = View.VISIBLE
                        rvNotifications.visibility = View.GONE
                    } else {
                        tvNoNotifications.visibility = View.GONE
                        rvNotifications.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        this@StudentNotificationActivity,
                        "Failed to load notifications",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@StudentNotificationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun markNotificationAsRead(notification: Notification) {
        if (notification.is_read == 1) return // Already read

        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            try {
                val response = apiService.markStudentNotificationAsRead(notification.notification_id)

                if (response.isSuccessful && response.body()?.success == true) {
                    // Update local notification status
                    val index = notifications.indexOfFirst { it.notification_id == notification.notification_id }
                    if (index != -1) {
                        notifications[index] = notification.copy(is_read = 1)
                        notificationAdapter.notifyItemChanged(index)

                        // Update unread count
                        val unreadCount = notifications.count { it.is_read == 0 }
                        updateUnreadCount(unreadCount)
                    }
                }
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
        if (::notificationAdapter.isInitialized) {
            loadNotifications()
        }
    }
}
