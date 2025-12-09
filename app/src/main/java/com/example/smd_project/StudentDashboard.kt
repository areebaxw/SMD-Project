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
import com.example.smd_project.activities.CourseRegistrationActivity
import com.example.smd_project.adapters.AnnouncementAdapter
import com.example.smd_project.adapters.TodayClassAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class StudentDashboard : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    
    // Views
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvStudentName: TextView
    private lateinit var tvRollNo: TextView
    private lateinit var tvSGPA: TextView
    private lateinit var tvCGPA: TextView
    private lateinit var tvAttendancePercentage: TextView
    private lateinit var rvTodayClasses: RecyclerView
    private lateinit var rvAnnouncements: RecyclerView
    private lateinit var menuIcon: ImageView
    private lateinit var notificationIcon: ImageView
    
    // Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    
    // Action buttons
    private lateinit var btnCoursesAction: View
    private lateinit var btnMarksAction: View
    private lateinit var btnEvaluationsAction: View
    private lateinit var btnFeesAction: View
    
    // Adapters
    private lateinit var todayClassAdapter: TodayClassAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studentdashboard)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerViews()
        setupDrawer()
        setupClickListeners()
        loadDashboardData()
    }
    
    private fun initViews() {
        // Profile Views
        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvStudentName = findViewById(R.id.tvStudentName)
        tvRollNo = findViewById(R.id.tvRollNo)
        tvSGPA = findViewById(R.id.tvSGPA)
        tvCGPA = findViewById(R.id.tvCGPA)
        tvAttendancePercentage = findViewById(R.id.tvAttendancePercentage)
        
        // RecyclerViews
        rvTodayClasses = findViewById(R.id.rvTodayClasses)
        rvAnnouncements = findViewById(R.id.rvAnnouncements)
        
        // Icons
        menuIcon = findViewById(R.id.menuIcon)
        notificationIcon = findViewById(R.id.notificationIcon)
        
        // Action buttons
        btnCoursesAction = findViewById(R.id.btnCoursesAction)
        btnMarksAction = findViewById(R.id.btnMarksAction)
        btnEvaluationsAction = findViewById(R.id.btnEvaluationsAction)
        btnFeesAction = findViewById(R.id.btnFeesAction)
        
        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_drawer)
        
        // Load profile picture
        val profileUrl = sessionManager.getProfilePic()
        if (!profileUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(profileUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(ivProfilePic)
        }
        
        tvStudentName.text = sessionManager.getUserName()
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
        // Update drawer header with student info
        try {
            val drawerHeaderContainer = navigationView.getHeaderView(0)
            val drawerUserName = drawerHeaderContainer.findViewById<TextView>(R.id.drawer_user_name)
            val drawerRollNo = drawerHeaderContainer.findViewById<TextView>(R.id.drawer_roll_no)
            val drawerEmail = drawerHeaderContainer.findViewById<TextView>(R.id.drawer_email)
            val drawerProfilePic = drawerHeaderContainer.findViewById<ImageView>(R.id.drawer_profile_pic)
            
            drawerUserName.text = sessionManager.getUserName()
            drawerRollNo.text = sessionManager.getUserId().toString()
            drawerEmail.text = sessionManager.getUserEmail()
            
            val profileUrl = sessionManager.getProfilePic()
            if (!profileUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(drawerProfilePic)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Handle navigation items
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_dashboard -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_registration -> {
                    startActivity(Intent(this, CourseRegistrationActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_courses -> {
                    startActivity(Intent(this, CourseListActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_marks -> {
                    startActivity(Intent(this, StudentMarksActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_evaluations -> {
                    startActivity(Intent(this, StudentEvaluationsActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_attendance -> {
                    startActivity(Intent(this, StudentAttendanceActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_fees -> {
                    startActivity(Intent(this, StudentFeesActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_announcements -> {
                    startActivity(Intent(this, AnnouncementListActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupClickListeners() {
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        
        // Quick action buttons
        btnCoursesAction.setOnClickListener {
            startActivity(Intent(this, CourseListActivity::class.java))
        }
        
        btnMarksAction.setOnClickListener {
            startActivity(Intent(this, StudentMarksActivity::class.java))
        }
        
        btnEvaluationsAction.setOnClickListener {
            startActivity(Intent(this, StudentEvaluationsActivity::class.java))
        }
        
        btnFeesAction.setOnClickListener {
            startActivity(Intent(this, StudentFeesActivity::class.java))
        }
        
        notificationIcon.setOnClickListener {
            Toast.makeText(this, "Opening Notifications", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<TextView>(R.id.viewAllClasses)?.setOnClickListener {
            startActivity(Intent(this, StudentAttendanceActivity::class.java))
        }
        
        findViewById<TextView>(R.id.viewAllAnnouncements)?.setOnClickListener {
            startActivity(Intent(this, AnnouncementListActivity::class.java))
        }
    }
    
    private fun loadDashboardData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStudentDashboard()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val dashboard = response.body()?.data
                    
                    dashboard?.let {
                        // Update UI with dashboard data
                        tvStudentName.text = it.student.full_name
                        tvRollNo.text = it.student.roll_no
                        tvSGPA.text = String.format("%.2f", it.sgpa)
                        tvCGPA.text = String.format("%.2f", it.cgpa)
                        tvAttendancePercentage.text = String.format("%.0f%%", it.attendance_percentage)
                        
                        // Load profile picture
                        it.student.profile_picture_url?.let { url ->
                            if (url.isNotEmpty()) {
                                Picasso.get()
                                    .load(url)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivProfilePic)
                            }
                        }
                        
                        // Update RecyclerViews
                        if (it.today_classes.isNotEmpty()) {
                            todayClassAdapter.updateClasses(it.today_classes)
                        }
                        if (it.announcements.isNotEmpty()) {
                            announcementAdapter.updateAnnouncements(it.announcements)
                        }
                    }
                } else {
                    Toast.makeText(this@StudentDashboard, 
                        response.body()?.message ?: "Failed to load dashboard", 
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentDashboard, 
                    "Error: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
                e.printStackTrace()
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
    }
}
