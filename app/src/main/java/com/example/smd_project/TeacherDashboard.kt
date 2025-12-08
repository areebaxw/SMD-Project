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
    private lateinit var tvPendingTasksCount: TextView
    private lateinit var rvTodayClasses: RecyclerView
    private lateinit var rvRecentActivity: RecyclerView
    
    private var rvCourses: RecyclerView? = null
    private var rvAnnouncements: RecyclerView? = null
    private var rvNotifications: RecyclerView? = null
    
    private lateinit var todayClassAdapter: TodayClassAdapter
    private var courseAdapter: CourseAdapter? = null
    private var announcementAdapter: AnnouncementAdapter? = null
    private var notificationAdapter: NotificationAdapter? = null
    
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
            tvPendingTasksCount = findViewById(R.id.tvPendingTasksCount)
            rvTodayClasses = findViewById(R.id.rvTodayClasses)
            rvRecentActivity = findViewById(R.id.rvRecentActivity)
            
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
        
        // Setup recent activity (announcements)
        announcementAdapter = AnnouncementAdapter(emptyList())
        rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(this@TeacherDashboard)
            adapter = announcementAdapter
        }
        
        // Setup other adapters if RecyclerViews exist
        rvCourses?.let {
            courseAdapter = CourseAdapter(emptyList()) { course ->
                onCourseClicked(course)
            }
            it.apply {
                layoutManager = LinearLayoutManager(this@TeacherDashboard, LinearLayoutManager.HORIZONTAL, false)
                adapter = courseAdapter
            }
        }
        
        rvAnnouncements?.let {
            announcementAdapter = AnnouncementAdapter(emptyList())
            it.apply {
                layoutManager = LinearLayoutManager(this@TeacherDashboard)
                adapter = announcementAdapter
            }
        }
        
        rvNotifications?.let {
            notificationAdapter = NotificationAdapter(emptyList())
            it.apply {
                layoutManager = LinearLayoutManager(this@TeacherDashboard)
                adapter = notificationAdapter
            }
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupSwipeRefresh() {
        // SwipeRefreshLayout not available in current layout
        // Pull-to-refresh can be added later by including SwipeRefreshLayout in activity_teacherdashboard.xml
    }
    
    private fun loadDashboardData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        // Set initial data from session while loading
        tvTeacherName.text = sessionManager.getUserName() ?: "Teacher"
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherDashboard()
                
                android.util.Log.d("TeacherDashboard", "Dashboard response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    android.util.Log.d("TeacherDashboard", "Dashboard body: $body")
                    
                    if (body?.success == true) {
                        val dashboard = body.data
                        
                        dashboard?.let {
                            // Check if teacher data is null - use SessionManager as fallback
                            if (it.teacher != null) {
                                android.util.Log.d("TeacherDashboard", "Teacher: ${it.teacher.full_name}")
                                
                                tvTeacherName.text = it.teacher.full_name
                                tvEmployeeId.text = it.teacher.email
                                
                                it.teacher.profile_picture_url?.let { url ->
                                    if (url.isNotEmpty()) {
                                        Picasso.get()
                                            .load(url)
                                            .placeholder(R.drawable.ic_launcher_foreground)
                                            .error(R.drawable.ic_launcher_foreground)
                                            .into(ivProfilePic)
                                    }
                                }
                            } else {
                                // Backend didn't return teacher info, use SessionManager
                                android.util.Log.d("TeacherDashboard", "Backend teacher=null, using SessionManager")
                                tvTeacherName.text = sessionManager.getUserName() ?: "Teacher"
                                tvEmployeeId.text = sessionManager.getUserEmail() ?: "N/A"
                                
                                val profileUrl = sessionManager.getProfilePic()
                                if (!profileUrl.isNullOrEmpty()) {
                                    Picasso.get()
                                        .load(profileUrl)
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.ic_launcher_foreground)
                                        .into(ivProfilePic)
                                }
                            }
                            
                            // Update course count
                            val courseCount = it.courses?.size ?: 0
                            tvCourseCount.text = courseCount.toString()
                            
                            // Update stats from API if available
                            it.stats?.let { stats ->
                                android.util.Log.d("TeacherDashboard", "Stats: students=${stats.total_students}, courses=${stats.total_courses}, pending=${stats.pending_tasks}")
                                tvStudentCount.text = stats.total_students.toString()
                                tvCourseCount.text = stats.total_courses.toString()
                                tvPendingTasksCount.text = stats.pending_tasks.toString()
                            } ?: run {
                                // Fallback: Calculate total students from courses data
                                android.util.Log.d("TeacherDashboard", "Stats not available in API response, using fallback")
                                tvCourseCount.text = courseCount.toString()
                                // This will be updated when courses data loads below
                            }
                            
                            // Update today's schedule if available
                            it.todaySchedule?.let { schedule ->
                                android.util.Log.d("TeacherDashboard", "Today's classes: ${schedule.size}")
                                if (schedule.isNotEmpty()) {
                                    todayClassAdapter.updateClasses(schedule)
                                }
                            }
                        }
                    } else {
                        android.util.Log.e("TeacherDashboard", "API success is false: ${body?.message}")
                        // Fallback to SessionManager data
                        tvTeacherName.text = sessionManager.getUserName() ?: "Teacher"
                        tvEmployeeId.text = sessionManager.getUserEmail() ?: "N/A"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("TeacherDashboard", "Dashboard API error: ${response.code()} - $errorBody")
                    Toast.makeText(this@TeacherDashboard, "Failed to load dashboard", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("TeacherDashboard", "Dashboard load error: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(this@TeacherDashboard, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            
            // Always try to load additional data regardless of dashboard API result
            loadCoursesData()
            loadAnnouncementsData()
            loadNotificationsData()
        }
    }
    
    private fun loadCoursesData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherCourses()
                
                android.util.Log.d("TeacherDashboard", "Courses response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    
                    if (body?.success == true) {
                        val courses = body.data ?: emptyList()
                        android.util.Log.d("TeacherDashboard", "Loaded ${courses.size} courses")
                        
                        if (courses.isNotEmpty()) {
                            tvCourseCount.text = courses.size.toString()
                            courseAdapter?.updateCourses(courses)
                            
                            // Calculate total enrolled students from courses
                            val totalStudents = courses.sumOf { it.enrolled_students ?: 0 }
                            android.util.Log.d("TeacherDashboard", "Total enrolled students: $totalStudents")
                            tvStudentCount.text = totalStudents.toString()
                        } else {
                            android.util.Log.d("TeacherDashboard", "No courses returned from backend")
                            tvCourseCount.text = "0"
                            tvStudentCount.text = "0"
                        }
                    } else {
                        android.util.Log.e("TeacherDashboard", "Courses API success is false: ${body?.message}")
                        tvCourseCount.text = "0"
                        tvStudentCount.text = "0"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("TeacherDashboard", "Courses API error: ${response.code()} - $errorBody")
                    // Show error but don't crash
                    Toast.makeText(this@TeacherDashboard, "Could not load courses", Toast.LENGTH_SHORT).show()
                    tvStudentCount.text = "0"
                }
            } catch (e: Exception) {
                android.util.Log.e("TeacherDashboard", "Courses load error: ${e.message}", e)
                tvCourseCount.text = "0"
                tvStudentCount.text = "0"
            }
        }
    }
    
    private fun loadAnnouncementsData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherAnnouncements()
                
                android.util.Log.d("TeacherDashboard", "Announcements response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    
                    if (body?.success == true) {
                        val announcements = body.data ?: emptyList()
                        android.util.Log.d("TeacherDashboard", "Loaded ${announcements.size} announcements")
                        
                        if (announcements.isNotEmpty()) {
                            // Show only recent 5
                            val recentAnnouncements = announcements.take(5)
                            announcementAdapter?.updateAnnouncements(recentAnnouncements)
                        } else {
                            android.util.Log.d("TeacherDashboard", "No announcements returned from backend")
                        }
                    } else {
                        android.util.Log.e("TeacherDashboard", "Announcements API success is false: ${body?.message}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("TeacherDashboard", "Announcements API error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                android.util.Log.e("TeacherDashboard", "Announcements load error: ${e.message}", e)
            }
        }
    }
    
    private fun loadNotificationsData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherNotifications()
                
                android.util.Log.d("TeacherDashboard", "Notifications response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    
                    if (body?.success == true) {
                        val notificationList = body.data ?: emptyList()
                        android.util.Log.d("TeacherDashboard", "Loaded ${notificationList.size} notifications")
                        
                        if (notificationList.isNotEmpty()) {
                            // Show only recent 5 unread
                            val unreadNotifications = notificationList.filter { it.is_read == 0 }.take(5)
                            notificationAdapter?.updateNotifications(unreadNotifications)
                        } else {
                            android.util.Log.d("TeacherDashboard", "No notifications returned from backend")
                        }
                    } else {
                        android.util.Log.e("TeacherDashboard", "Notifications API success is false: ${body?.message}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("TeacherDashboard", "Notifications API error: ${response.code()} - $errorBody")
                    // Don't show toast for notifications - less critical
                }
            } catch (e: Exception) {
                android.util.Log.e("TeacherDashboard", "Notifications load error: ${e.message}", e)
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
