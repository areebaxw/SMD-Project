package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.CourseAdapter
import com.example.smd_project.models.Course
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.repository.TeacherRepository
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class CourseListTeacherActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var repository: TeacherRepository
    private lateinit var rvCourses: RecyclerView
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)

        sessionManager = SessionManager(this)
        repository = TeacherRepository(this)

        initViews()
        setupRecyclerView()
        loadCourses()
    }

    private fun initViews() {
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
            layoutManager = LinearLayoutManager(this@CourseListTeacherActivity)
            adapter = courseAdapter
        }
    }

    private fun loadCourses() {
        lifecycleScope.launch {
            try {
                // Use repository for offline support
                val isOnline = NetworkUtils.isOnline(this@CourseListTeacherActivity)
                val result = repository.getCourses(forceRefresh = isOnline)
                
                result.onSuccess { courses ->
                    if (courses.isEmpty()) {
                        Toast.makeText(
                            this@CourseListTeacherActivity,
                            "No courses registered yet",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        courseAdapter.updateCourses(courses)
                        if (!isOnline) {
                            Toast.makeText(
                                this@CourseListTeacherActivity,
                                "Showing cached data (offline)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.onFailure { error ->
                    Toast.makeText(
                        this@CourseListTeacherActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CourseListTeacherActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun navigateToCourseDetail(course: Course) {
        val intent = Intent(this, CourseDetailsActivity::class.java).apply {
            putExtra("course_id", course.course_id)
            putExtra("course_name", course.course_name)
            putExtra("course_code", course.course_code)
        }
        startActivity(intent)
    }
}
