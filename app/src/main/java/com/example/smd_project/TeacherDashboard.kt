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
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val dashboard = response.body()?.data
                    
                    dashboard?.let {
                        tvTeacherName.text = it.teacher.full_name
                        tvEmployeeId.text = it.teacher.email
                        
                        it.teacher.profile_image?.let { url ->
                            Picasso.get()
                                .load(url)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .into(ivProfilePic)
                        }
                        
                        // Update today's schedule if available
                        it.todaySchedule?.let { schedule ->
                            if (schedule.isNotEmpty()) {
                                todayClassAdapter.updateClasses(schedule)
                            }
                        }
                    }
                } else {
                    // API error - log it but don't show error to user if UI is working
                    val errorCode = response.code()
                    android.util.Log.e("TeacherDashboard", "Dashboard API error: $errorCode")
                }
            } catch (e: Exception) {
                android.util.Log.e("TeacherDashboard", "Dashboard load error: ${e.message}")
                e.printStackTrace()
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
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val courses = response.body()?.data ?: emptyList()
                    courseAdapter?.updateCourses(courses)
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
                    announcementAdapter?.updateAnnouncements(recentAnnouncements)
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
                    val notificationList = response.body()?.data ?: emptyList()
                    // Show only recent 5 unread
                    val unreadNotifications = notificationList.filter { it.is_read == 0 }.take(5)
                    notificationAdapter?.updateNotifications(unreadNotifications)
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
