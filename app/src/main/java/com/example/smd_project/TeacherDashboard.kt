package com.example.smd_project

import DrawerAdapter
import android.content.Intent
import android.os.Bundle
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
import com.example.smd_project.models.DrawerItem
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import com.example.smd_project.utils.ActivityManager
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
    private var activityAdapter: TeacherActivityAdapter? = null
    private lateinit var activityManager: ActivityManager

    // Drawer views
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacherdashboard)

        sessionManager = SessionManager(this)
        activityManager = ActivityManager(this)

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
            
            // Make profile picture circular
            ivProfilePic.clipToOutline = true
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

        // Setup recent activity with new TeacherActivityAdapter
        activityAdapter = TeacherActivityAdapter(emptyList()) { activity ->
            showActivityDetailsDialog(activity)
        }
        rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(this@TeacherDashboard)
            adapter = activityAdapter
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
            DrawerItem("Dashboard", R.drawable.dashboard),
            DrawerItem("Announcements", R.drawable.announcements),
            DrawerItem("Marks", R.drawable.test),
            DrawerItem("Attendance", R.drawable.attendance),
            DrawerItem("Courses", R.drawable.courses),
            DrawerItem("Schedule", R.drawable.schedule),
            DrawerItem("Upload Grades", R.drawable.upload_grades),
                    DrawerItem("Logout", R.drawable.logout)

        )

        val drawerAdapter = DrawerAdapter(drawerItems) { item ->
            when (item.title) {
                "Dashboard" -> { /* maybe reload current activity */ }
                "Announcements" -> startActivity(Intent(this, AnnouncementListActivity::class.java))
                "Marks" -> startActivity(Intent(this, EnterMarks::class.java))
                "Attendance" -> startActivity(Intent(this, MarkAttendance::class.java))
                "Courses" -> startActivity(Intent(this, CourseListTeacherActivity::class.java))
                "Schedule" -> startActivity(Intent(this, ScheduleActivity::class.java))
                "Upload Grades" -> startActivity(Intent(this, UploadGrade::class.java))
                "Logout" -> performLogout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        drawerRecyclerView.adapter = drawerAdapter
        drawerRecyclerView.layoutManager = LinearLayoutManager(this)

        // Populate header
        val drawerUserName = findViewById<TextView>(R.id.drawer_user_name)
        val drawerEmail = findViewById<TextView>(R.id.drawer_email)
        val drawerTeacherId = findViewById<TextView>(R.id.drawer_teacher_id)
        val drawerProfilePic = findViewById<ImageView>(R.id.drawer_profile_pic)
        drawerUserName.text = sessionManager.getUserName()
        drawerEmail.text = sessionManager.getUserEmail()
        drawerTeacherId.text = "ID: ${sessionManager.getUserId()}"
        Picasso.get().load(sessionManager.getProfilePic())
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(drawerProfilePic)

        // Menu icon opens drawer
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

            findViewById<View>(R.id.btnViewAllSchedule)?.setOnClickListener {
                startActivity(Intent(this, ScheduleActivity::class.java))
            }

            findViewById<ImageView>(R.id.btnClearActivity)?.setOnClickListener {
                clearAllActivities()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearAllActivities() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear Activities")
            .setMessage("Are you sure you want to clear all activities?")
            .setPositiveButton("Yes") { _, _ ->
                activityManager.clearAllActivities()
                activityAdapter?.updateActivities(emptyList())
                Toast.makeText(this, "Activities cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showActivityDetailsDialog(activity: com.example.smd_project.models.TeacherActivity) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_activity_details, null)

        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvDialogDescription = dialogView.findViewById<TextView>(R.id.tvDialogDescription)
        val tvDialogTime = dialogView.findViewById<TextView>(R.id.tvDialogTime)
        val btnDeleteActivity = dialogView.findViewById<TextView>(R.id.btnDeleteActivity)
        val btnCloseDialog = dialogView.findViewById<TextView>(R.id.btnCloseDialog)

        tvDialogTitle.text = activity.title
        tvDialogDescription.text = activity.description
        tvDialogTime.text = activity.getFormattedTime()

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        btnDeleteActivity.setOnClickListener {
            activityManager.removeActivity(activity.activity_id)
            val updatedActivities = activityManager.getRecentActivities(limit = 5)
            activityAdapter?.updateActivities(updatedActivities)
            dialog.dismiss()
            Toast.makeText(this@TeacherDashboard, "Activity deleted", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }
    private fun performLogout() {
        // Clear session
        sessionManager.clearSession()

        // Redirect to LoginActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
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
                            it.teacher?.let { teacher ->
                                tvTeacherName.text = teacher.full_name
                                tvEmployeeId.text = teacher.email
                                teacher.profile_picture_url?.let { url ->
                                    if (url.isNotEmpty()) {
                                        Picasso.get()
                                            .load(url)
                                            .placeholder(R.drawable.ic_launcher_foreground)
                                            .error(R.drawable.ic_launcher_foreground)
                                            .into(ivProfilePic)
                                    }
                                }
                            } ?: run {
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
            loadRecentActivitiesData()
        }
    }

    private fun loadRecentActivitiesData() {
        try {
            val activities = activityManager.getRecentActivities(limit = 5)
            if (activities.isNotEmpty()) {
                activityAdapter?.updateActivities(activities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                        } else {
                            tvCourseCount.text = "0"
                        }
                    } else {
                        tvCourseCount.text = "0"
                    }
                }
            } catch (_: Exception) {
                tvCourseCount.text = "0"
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
        loadRecentActivitiesData()
    }
}
