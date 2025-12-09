package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.adapters.*
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

    // Drawer views
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacherdashboard)

        sessionManager = SessionManager(this)

        initViews()
        setupRecyclerViews()
        setupDrawer()
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

            drawerLayout = findViewById(R.id.drawer_layout)
            drawerRecyclerView = findViewById(R.id.drawerRecyclerView)

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

    private fun setupDrawer() {
        val drawerItems = listOf(
            DrawerItem("Announcements", R.drawable.postannouncement_icon),
            DrawerItem("Marks", R.drawable.entermarks_icon),
            DrawerItem("Attendance", R.drawable.greentick),
            DrawerItem("Courses", R.drawable.notifybutton),
            DrawerItem("Schedule", R.drawable.notifybutton)
        )

        val drawerAdapter = DrawerAdapter(drawerItems) { item ->
            when(item.title) {
                "Announcements" -> {  val intent = Intent(this, AnnouncementListActivity::class.java)
                    startActivity(intent) }
                "Marks" -> { val intent = Intent(this, EnterMarks::class.java)
                    startActivity(intent) }
                "Attendance" -> {val intent = Intent(this, MarkAttendance::class.java)
                    startActivity(intent)}
                "Courses" -> { val intent = Intent(this, CourseListActivity::class.java)
                    startActivity(intent)}
                "Schedule" -> { val intent = Intent(this, ScheduleActivity::class.java)
                    startActivity(intent) }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerRecyclerView.adapter = drawerAdapter
        drawerRecyclerView.layoutManager = LinearLayoutManager(this)

        // Menu icon click opens the drawer
        findViewById<View>(R.id.menu_icon)?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
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
    }

    private fun loadDashboardData() {
        val apiService = RetrofitClient.getApiService(sessionManager)

        tvTeacherName.text = sessionManager.getUserName() ?: "Teacher"

        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherDashboard()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        val dashboard = body.data
                        dashboard?.let {
                            if (it.teacher != null) {
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

                            val courseCount = it.courses?.size ?: 0
                            tvCourseCount.text = courseCount.toString()

                            it.stats?.let { stats ->
                                tvStudentCount.text = stats.total_students.toString()
                                tvCourseCount.text = stats.total_courses.toString()
                                tvPendingTasksCount.text = stats.pending_tasks.toString()
                            } ?: run {
                                tvCourseCount.text = courseCount.toString()
                            }

                            it.todaySchedule?.let { schedule ->
                                if (schedule.isNotEmpty()) {
                                    todayClassAdapter.updateClasses(schedule)
                                }
                            }
                        }
                    } else {
                        tvTeacherName.text = sessionManager.getUserName() ?: "Teacher"
                        tvEmployeeId.text = sessionManager.getUserEmail() ?: "N/A"
                    }
                } else {
                    Toast.makeText(this@TeacherDashboard, "Failed to load dashboard", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TeacherDashboard, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

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
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        val courses = body.data ?: emptyList()
                        if (courses.isNotEmpty()) {
                            tvCourseCount.text = courses.size.toString()
                            courseAdapter?.updateCourses(courses)
                            val totalStudents = courses.sumOf { it.enrolled_students ?: 0 }
                            tvStudentCount.text = totalStudents.toString()
                        } else {
                            tvCourseCount.text = "0"
                            tvStudentCount.text = "0"
                        }
                    } else {
                        tvCourseCount.text = "0"
                        tvStudentCount.text = "0"
                    }
                } else {
                    tvStudentCount.text = "0"
                }
            } catch (_: Exception) {
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
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        val announcements = body.data ?: emptyList()
                        if (announcements.isNotEmpty()) {
                            val recentAnnouncements = announcements.take(5)
                            announcementAdapter?.updateAnnouncements(recentAnnouncements)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadNotificationsData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherNotifications()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        val notificationList = body.data ?: emptyList()
                        if (notificationList.isNotEmpty()) {
                            val unreadNotifications = notificationList.filter { it.is_read == 0 }.take(5)
                            notificationAdapter?.updateNotifications(unreadNotifications)
                        }
                    }
                }
            } catch (_: Exception) {}
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
        loadDashboardData()
    }
}
