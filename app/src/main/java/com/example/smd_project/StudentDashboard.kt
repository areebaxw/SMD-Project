package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.smd_project.activities.AssignmentsActivity
import com.example.smd_project.activities.CourseRegistrationActivity
import com.example.smd_project.activities.StudentNotificationActivity
import com.example.smd_project.activities.StudentTranscriptActivity
import com.example.smd_project.adapters.AnnouncementAdapter
import DrawerAdapter
import com.example.smd_project.adapters.TodayClassAdapter
import com.example.smd_project.models.DrawerItem
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.repository.StudentRepository
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import com.example.smd_project.utils.SyncManager
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class StudentDashboard : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: StudentRepository
    
    // Views
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvStudentName: TextView
    private lateinit var tvRollNo: TextView
    private lateinit var tvCGPA: TextView
    private lateinit var tvAttendancePercentage: TextView
    private lateinit var rvTodayClasses: RecyclerView
    private lateinit var rvAnnouncements: RecyclerView
    private lateinit var menuIcon: ImageView
    private lateinit var notificationIcon: ImageView
    private lateinit var tvNotificationBadge: TextView
    private lateinit var tvOfflineIndicator: TextView
    
    // Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerRecyclerView: RecyclerView
    
    // Action buttons
    private lateinit var btnCoursesAction: View
    private lateinit var btnEvaluationsAction: View
    private lateinit var btnFeesAction: View
    private lateinit var btnAttendanceAction: View
    
    // Adapters
    private lateinit var todayClassAdapter: TodayClassAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studentdashboard)
        
        sessionManager = SessionManager(this)
        repository = StudentRepository(this)
        
        initViews()
        setupRecyclerViews()
        setupDrawer()
        setupClickListeners()
        setupSwipeRefresh()
        setupFCMToken()
        setupPeriodicSync()
        loadDashboardData()
        observeUnreadNotifications()
    }
    
    private fun initViews() {
        // Profile Views
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvRollNo = findViewById(R.id.tvRollNo)
        tvCGPA = findViewById(R.id.tvCGPA)
        tvAttendancePercentage = findViewById(R.id.tvAttendancePercentage)
        tvOfflineIndicator = findViewById(R.id.tvOfflineIndicator)
        
        // RecyclerViews
        rvTodayClasses = findViewById(R.id.rvTodayClasses)
        rvAnnouncements = findViewById(R.id.rvAnnouncements)
        
        // Icons
        menuIcon = findViewById(R.id.menuIcon)
        notificationIcon = findViewById(R.id.notificationIcon)
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge)
        
        // Action buttons
        btnCoursesAction = findViewById(R.id.btnCoursesAction)
        btnEvaluationsAction = findViewById(R.id.btnEvaluationsAction)
        btnFeesAction = findViewById(R.id.btnFeesAction)
        btnAttendanceAction = findViewById(R.id.btnAttendanceAction)
        
        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerRecyclerView = findViewById(R.id.drawerRecyclerView)
        
        // Update offline indicator
        updateOfflineIndicator()
        
        // Load profile picture
        val profileUrl = sessionManager.getProfilePic()
        if (!profileUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(profileUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(ivProfilePic)
        }
        
        // Make profile picture circular
        ivProfilePic.clipToOutline = true
    }
    
    private fun setupRecyclerViews() {
        // Today's Classes
        todayClassAdapter = TodayClassAdapter(emptyList())
        rvTodayClasses.apply {
            layoutManager = LinearLayoutManager(this@StudentDashboard)
            adapter = todayClassAdapter
        }
        
        // Announcements
        announcementAdapter = AnnouncementAdapter(emptyList())
        rvAnnouncements.apply {
            layoutManager = LinearLayoutManager(this@StudentDashboard)
            adapter = announcementAdapter
        }
    }
    
    private fun setupDrawer() {
        val drawerItems = listOf(
            DrawerItem("Dashboard", R.drawable.dashboard),
            DrawerItem("Course Registration", R.drawable.register),
            DrawerItem("My Courses", R.drawable.courses),
            DrawerItem("Evaluations", R.drawable.evaluations),
            DrawerItem("Attendance", R.drawable.attendance),
            DrawerItem("Fees", R.drawable.fees),
            DrawerItem("Announcements", R.drawable.announcements),
            DrawerItem("Transcript", R.drawable.marks),
            DrawerItem("Logout", R.drawable.logout)
        )

        val drawerAdapter = DrawerAdapter(drawerItems) { item ->
            when (item.title) {
                "Dashboard" -> { /* maybe reload current activity */ }
                "Course Registration" -> startActivity(Intent(this, CourseRegistrationActivity::class.java))
                "My Courses" -> startActivity(Intent(this, CourseListStudentActivity::class.java))
                "Evaluations" -> startActivity(Intent(this, AssignmentsActivity::class.java))
                "Attendance" -> startActivity(Intent(this, StudentAttendanceActivity::class.java))
                "Fees" -> startActivity(Intent(this, StudentFeesActivity::class.java))
                "Announcements" -> startActivity(Intent(this, AnnouncementListActivity::class.java))
                "Transcript" -> startActivity(Intent(this, StudentTranscriptActivity::class.java))
                "Logout" -> performLogout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerRecyclerView.adapter = drawerAdapter
        drawerRecyclerView.layoutManager = LinearLayoutManager(this)

        // Populate header
        val drawerUserName = findViewById<TextView>(R.id.drawer_user_name)
        val drawerRollNo = findViewById<TextView>(R.id.drawer_roll_no)
        val drawerEmail = findViewById<TextView>(R.id.drawer_email)
        val drawerProfilePic = findViewById<ImageView>(R.id.drawer_profile_pic)
        drawerUserName.text = sessionManager.getUserName()
        drawerRollNo.text = sessionManager.getRollNo() ?: "N/A"
        drawerEmail.text = sessionManager.getUserEmail()
        Picasso.get().load(sessionManager.getProfilePic())
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(drawerProfilePic)

        // Menu icon opens drawer
        findViewById<View>(R.id.menuIcon)?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupClickListeners() {
        // Quick action buttons
        btnCoursesAction.setOnClickListener {
            startActivity(Intent(this, CourseListStudentActivity::class.java))
        }
        
        btnEvaluationsAction.setOnClickListener {
            startActivity(Intent(this, AssignmentsActivity::class.java))
        }
        
        btnFeesAction.setOnClickListener {
            startActivity(Intent(this, StudentFeesActivity::class.java))
        }
        
        btnAttendanceAction.setOnClickListener {
            startActivity(Intent(this, StudentAttendanceActivity::class.java))
        }
        
        notificationIcon.setOnClickListener {
            startActivity(Intent(this, StudentNotificationActivity::class.java))
        }
        
        findViewById<TextView>(R.id.viewAllClasses)?.setOnClickListener {
            startActivity(Intent(this, StudentScheduleActivity::class.java))
        }
        
        findViewById<TextView>(R.id.viewAllAnnouncements)?.setOnClickListener {
            startActivity(Intent(this, AnnouncementListActivity::class.java))
        }
    }
    
    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val result = repository.getDashboardData(forceRefresh = false)
                
                if (result.isSuccess) {
                    val dashboard = result.getOrNull()
                    dashboard?.let {
                        updateUI(it)
                        
                        // Show offline indicator if needed
                        if (!NetworkUtils.isOnline(this@StudentDashboard)) {
                            Toast.makeText(this@StudentDashboard, 
                                "Showing cached data (offline)", 
                                Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        // No cached data available
                        Toast.makeText(this@StudentDashboard, 
                            "No data available. Please connect to internet.", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("StudentDashboard", "Error loading data", e)
                Toast.makeText(this@StudentDashboard, 
                    "Error loading data", 
                    Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun updateUI(dashboardData: com.example.smd_project.models.StudentDashboard) {
        // Update UI with dashboard data
        tvStudentName.text = dashboardData.student.full_name
        tvRollNo.text = dashboardData.student.roll_no
        tvCGPA.text = String.format("%.2f", dashboardData.student.cgpa)
        tvAttendancePercentage.text = String.format("%.0f%%", dashboardData.attendance_percentage)
        
        // Load profile picture
        dashboardData.student.profile_picture_url?.let { url ->
            if (url.isNotEmpty()) {
                Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivProfilePic)
            }
        }
        
        // Update RecyclerViews
        android.util.Log.d("StudentDashboard", "Today's classes count: ${dashboardData.today_classes.size}")
        
        if (dashboardData.today_classes.isNotEmpty()) {
            todayClassAdapter.updateClasses(dashboardData.today_classes)
            rvTodayClasses.visibility = View.VISIBLE
        } else {
            android.util.Log.d("StudentDashboard", "No classes for today")
            rvTodayClasses.visibility = View.GONE
        }
        
        if (dashboardData.announcements.isNotEmpty()) {
            announcementAdapter.updateAnnouncements(dashboardData.announcements)
        }
    }
    
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            if (NetworkUtils.isOnline(this)) {
                refreshDashboard()
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun refreshDashboard() {
        lifecycleScope.launch {
            try {
                swipeRefreshLayout.isRefreshing = true
                
                // Force refresh from network
                val result = repository.getDashboardData(forceRefresh = true)
                
                if (result.isSuccess) {
                    val dashboard = result.getOrNull()
                    dashboard?.let {
                        updateUI(it)
                        Toast.makeText(this@StudentDashboard, "Data updated", Toast.LENGTH_SHORT).show()
                    }
                }
                
                updateOfflineIndicator()
            } catch (e: Exception) {
                android.util.Log.e("StudentDashboard", "Error refreshing", e)
                Toast.makeText(this@StudentDashboard, "Refresh failed", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun updateOfflineIndicator() {
        if (NetworkUtils.isOnline(this)) {
            tvOfflineIndicator.visibility = View.GONE
        } else {
            tvOfflineIndicator.visibility = View.VISIBLE
            tvOfflineIndicator.text = "Offline Mode"
        }
    }
    
    private fun setupPeriodicSync() {
        // Schedule background sync
        SyncManager.schedulePeriodicSync(this)
    }
    
    private fun observeUnreadNotifications() {
        repository.getUnreadNotificationCount().observe(this) { unreadCount ->
            if (unreadCount > 0) {
                tvNotificationBadge.visibility = View.VISIBLE
                tvNotificationBadge.text = if (unreadCount > 99) "99+" else unreadCount.toString()
            } else {
                tvNotificationBadge.visibility = View.GONE
            }
        }
    }
    
    private fun performLogout() {
        sessionManager.clearSession()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
    
    override fun onResume() {
        super.onResume()
        loadDashboardData()
        updateOfflineIndicator()
        
        // Trigger background sync if online
        if (NetworkUtils.isOnline(this)) {
            SyncManager.syncDashboard(this)
        }
    }
    
    private fun setupFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM token
            val token = task.result
            android.util.Log.d("FCM", "FCM Token: $token")

            // Send token to server
            registerFCMToken(token)
        }
    }

    private fun registerFCMToken(token: String) {
        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            try {
                if (!NetworkUtils.isOnline(this@StudentDashboard)) {
                    android.util.Log.d("FCM", "Offline - will register token later")
                    return@launch
                }
                
                val response = apiService.registerFCMToken(
                    mapOf(
                        "fcm_token" to token,
                        "device_type" to "android"
                    )
                )

                if (response.isSuccessful) {
                    sessionManager.saveFCMToken(token)
                    android.util.Log.d("FCM", "Token registered successfully")
                }
            } catch (e: Exception) {
                android.util.Log.e("FCM", "Error registering token: ${e.message}")
            }
        }
    }
}
