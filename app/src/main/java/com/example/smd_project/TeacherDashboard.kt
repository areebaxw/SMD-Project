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
import com.example.smd_project.adapters.CourseAdapter
import com.example.smd_project.adapters.AnnouncementAdapter
import com.example.smd_project.adapters.NotificationAdapter
import com.example.smd_project.models.Course
import com.example.smd_project.models.Announcement
import com.example.smd_project.models.Notification
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
    private lateinit var rvCourses: RecyclerView
    private lateinit var rvAnnouncements: RecyclerView
    private lateinit var rvNotifications: RecyclerView
    
    private lateinit var todayClassAdapter: TodayClassAdapter
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter
    private lateinit var notificationAdapter: NotificationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacherdashboard)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerViews()
        setupClickListeners()
        loadDashboardData()
        setupSwipeRefresh()
    }
    
    private fun initViews() {
        try {
            ivProfilePic = findViewById(R.id.ivProfilePic)
            tvTeacherName = findViewById(R.id.tvTeacherName)
            tvEmployeeId = findViewById(R.id.tvEmployeeId)
            tvCourseCount = findViewById(R.id.tvCourseCount)
            tvStudentCount = findViewById(R.id.tvStudentCount)
            rvTodayClasses = findViewById(R.id.rvTodayClasses)
            
            // Try to find additional RecyclerViews if they exist
            try {
                rvCourses = findViewById(R.id.rvCourses)
                rvAnnouncements = findViewById(R.id.rvAnnouncements)
                rvNotifications = findViewById(R.id.rvNotifications)
            } catch (e: Exception) {
                // Views may not be in XML yet
            }
            
            tvTeacherName.text = sessionManager.getUserName()
            
            val profileUrl = sessionManager.getProfilePic()
            if (!profileUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
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
        
        // Setup other adapters if RecyclerViews exist
        try {
            courseAdapter = CourseAdapter(emptyList()) { course ->
                onCourseClicked(course)
            }
            rvCourses.apply {
                layoutManager = LinearLayoutManager(this@TeacherDashboard, LinearLayoutManager.HORIZONTAL, false)
                adapter = courseAdapter
            }
        } catch (e: Exception) {
            // RecyclerView not found
        }
        
        try {
            announcementAdapter = AnnouncementAdapter(emptyList())
            rvAnnouncements.apply {
                layoutManager = LinearLayoutManager(this@TeacherDashboard)
                adapter = announcementAdapter
            }
        } catch (e: Exception) {
            // RecyclerView not found
        }
        
        try {
            notificationAdapter = NotificationAdapter(emptyList())
            rvNotifications.apply {
                layoutManager = LinearLayoutManager(this@TeacherDashboard)
                adapter = notificationAdapter
            }
        } catch (e: Exception) {
            // RecyclerView not found
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
            
            // View all courses button
            findViewById<View>(R.id.tvCoursesViewAll)?.setOnClickListener {
                startActivity(Intent(this, CourseListActivity::class.java))
            }
            
            // View all announcements button
            findViewById<View>(R.id.tvAnnouncementsViewAll)?.setOnClickListener {
                startActivity(Intent(this, AnnouncementListActivity::class.java))
            }
            
            // View all notifications button
            findViewById<View>(R.id.tvNotificationsViewAll)?.setOnClickListener {
                startActivity(Intent(this, NotificationActivity::class.java))
            }
            
            // View schedule button
            findViewById<View>(R.id.tvScheduleViewAll)?.setOnClickListener {
                startActivity(Intent(this, ScheduleActivity::class.java))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupSwipeRefresh() {
        try {
            val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)
            swipeRefresh?.setOnRefreshListener {
                loadDashboardData()
                swipeRefresh.isRefreshing = false
            }
        } catch (e: Exception) {
            // SwipeRefresh not found
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
                    
                    // Load additional data
                    loadCoursesData()
                    loadAnnouncementsData()
                    loadNotificationsData()
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
    
    private fun loadCoursesData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherCourses()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val courses = response.body()?.data ?: emptyList()
                    try {
                        courseAdapter.updateCourses(courses)
                    } catch (e: Exception) {
                        // Adapter not initialized
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadAnnouncementsData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherAnnouncements()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val announcements = response.body()?.data ?: emptyList()
                    // Show only recent 5
                    val recentAnnouncements = announcements.take(5)
                    try {
                        announcementAdapter.updateAnnouncements(recentAnnouncements)
                    } catch (e: Exception) {
                        // Adapter not initialized
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadNotificationsData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherNotifications()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val notificationResponse = response.body()?.data
                    notificationResponse?.let {
                        // Show only recent 5 unread
                        val unreadNotifications = it.notifications.filter { !it.is_read }.take(5)
                        try {
                            notificationAdapter.updateNotifications(unreadNotifications)
                        } catch (e: Exception) {
                            // Adapter not initialized
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun onCourseClicked(course: Course) {
        val intent = Intent(this, CourseDetailActivity::class.java).apply {
            putExtra("course_id", course.course_id)
            putExtra("course_name", course.course_name)
            putExtra("course_code", course.course_code)
        }
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to dashboard
        loadDashboardData()
    }
}
