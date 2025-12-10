package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.smd_project.adapters.CourseAdapter
import com.example.smd_project.models.Course
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.repository.StudentRepository
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class CourseListStudentActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: StudentRepository
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var rvCourses: RecyclerView
    private lateinit var courseAdapter: CourseAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)
        
        sessionManager = SessionManager(this)
        repository = StudentRepository(this)
        
        initViews()
        setupRecyclerView()
        setupSwipeRefresh()
        observeCourses()
    }
    
    private fun initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        rvCourses = findViewById(R.id.rvCourses)
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(emptyList()) { course ->
            navigateToCourseDetail(course)
        }
        rvCourses.apply {
            layoutManager = LinearLayoutManager(this@CourseListStudentActivity)
            adapter = courseAdapter
        }
    }
    
    private fun observeCourses() {
        repository.getCourses().observe(this) { enrollments ->
            val courses = enrollments.map { enrollment ->
                Course(
                    course_id = enrollment.course_id,
                    course_code = enrollment.course_code ?: "",
                    course_name = enrollment.course_name ?: "",
                    description = enrollment.description,
                    credit_hours = enrollment.credit_hours ?: 0,
                    semester = enrollment.semester?.toIntOrNull(),
                    is_required = 0,
                    is_active = 1,
                    instructors = enrollment.teacher_name,
                    schedule = null,
                    enrolled_students = null,
                    grade = enrollment.grade,
                    gpa = enrollment.gpa,
                    status = enrollment.status
                )
            }
            
            if (courses.isEmpty()) {
                Toast.makeText(
                    this@CourseListActivity,
                    "No courses registered yet",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                courseAdapter.updateCourses(courses)
            }
        }
        
        // Initial refresh
        refreshCourses()
    }
    
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshCourses()
        }
    }
    
    private fun refreshCourses() {
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
            return
        }
        
        lifecycleScope.launch {
            swipeRefreshLayout.isRefreshing = true
            repository.refreshCourses()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    
    private fun navigateToCourseDetail(course: Course) {
        val intent = Intent(this, CourseDetailActivity::class.java).apply {
            putExtra("course_id", course.course_id)
            putExtra("course_name", course.course_name)
            putExtra("course_code", course.course_code)
        }
        startActivity(intent)
    }
}
