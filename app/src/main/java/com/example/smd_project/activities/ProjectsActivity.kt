package com.example.smd_project.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.adapters.CourseSelectorAdapter
import com.example.smd_project.adapters.StudentMarkDisplayAdapter
import com.example.smd_project.models.Mark
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class ProjectsActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentMarkDisplayAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var currentCourseName: TextView
    private lateinit var courseDropdownCard: View
    
    private var allMarks: List<Mark> = emptyList()
    private var selectedCourseCode: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)
        
        sessionManager = SessionManager(this)
        setupViews()
        setupRecyclerView()
        setupClickListeners()
        loadMarksData()
    }
    
    private fun setupViews() {
        currentCourseName = findViewById(R.id.current_course_name)
        courseDropdownCard = findViewById(R.id.course_dropdown_card)
        
        // Highlight current tab - Projects
        findViewById<TextView>(R.id.tab_projects).apply {
            setTextColor(getColor(R.color.purple_700))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvProjects)
        adapter = StudentMarkDisplayAdapter(emptyList())
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProjectsActivity)
            adapter = this@ProjectsActivity.adapter
        }
    }
    
    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.back_icon).setOnClickListener {
            finish()
        }
        
        // Tab navigation
        findViewById<TextView>(R.id.tab_assignments).setOnClickListener {
            navigateToActivity(AssignmentsActivity::class.java)
        }
        
        findViewById<TextView>(R.id.tab_quizzes).setOnClickListener {
            navigateToActivity(QuizActivity::class.java)
        }
        
        findViewById<TextView>(R.id.tab_sessionals).setOnClickListener {
            navigateToActivity(SessionalsActivity::class.java)
        }
        
        findViewById<TextView>(R.id.tab_finals).setOnClickListener {
            navigateToActivity(FinalsActivity::class.java)
        }
    }
    
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        selectedCourseCode?.let { intent.putExtra("selectedCourse", it) }
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
    
    private fun loadMarksData() {
        // Check network before making API call
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "No internet connection. Marks require online access.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStudentMarks()
                if (response.isSuccessful && response.body()?.success == true) {
                    allMarks = response.body()?.data ?: emptyList()
                    setupCourseFilter()
                } else {
                    Toast.makeText(this@ProjectsActivity, "Failed to load marks", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProjectsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupCourseFilter() {
        val courses = allMarks.map { "${it.course_code} - ${it.course_name}" }.distinct()
        
        if (courses.isEmpty()) {
            currentCourseName.text = "No courses available"
            return
        }
        
        val savedCourse = intent.getStringExtra("selectedCourse")
        val selectedIndex = if (savedCourse != null) {
            courses.indexOfFirst { it.startsWith(savedCourse) }.takeIf { it >= 0 } ?: 0
        } else 0
        
        selectedCourseCode = courses[selectedIndex].split(" - ")[0]
        currentCourseName.text = courses[selectedIndex].split(" - ")[1]
        filterMarksByCourseAndType()
        
        courseDropdownCard.setOnClickListener {
            showCourseSelectionDialog(courses)
        }
    }
    
    private fun showCourseSelectionDialog(courses: List<String>) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_course_selector)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        val rvCourses = dialog.findViewById<RecyclerView>(R.id.rvCourses)
        val courseList = courses.map { 
            val parts = it.split(" - ")
            Pair(parts[0], parts.getOrNull(1) ?: "")
        }
        
        val adapter = CourseSelectorAdapter(courseList) { position ->
            selectedCourseCode = courses[position].split(" - ")[0]
            currentCourseName.text = courses[position].split(" - ")[1]
            filterMarksByCourseAndType()
            dialog.dismiss()
        }
        
        rvCourses.layoutManager = LinearLayoutManager(this)
        rvCourses.adapter = adapter
        
        dialog.show()
    }
    
    private fun filterMarksByCourseAndType() {
        val filtered = allMarks.filter { mark ->
            mark.course_code == selectedCourseCode &&
            mark.evaluation_type?.contains("Project", ignoreCase = true) == true
        }
        adapter.updateMarks(filtered)
        
        if (filtered.isEmpty()) {
            Toast.makeText(this, "No projects for this course", Toast.LENGTH_SHORT).show()
        }
    }
}
