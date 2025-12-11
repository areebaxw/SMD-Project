package com.example.smd_project

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.database.AppDatabase
import com.example.smd_project.models.PostAnnouncementRequest
import com.example.smd_project.models.Course
import com.example.smd_project.models.TeacherActivity
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import com.example.smd_project.utils.ActivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostAnnouncement : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var spinnerCourse: Spinner
    private lateinit var spinnerType: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnPublish: TextView

    private var selectedCourseId: Int? = null
    private var selectedType: String? = null
    private var courses: List<Course> = emptyList()

    private val announcementTypes = listOf("General", "Urgent", "Reminder") // Example types

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postannouncement)

        sessionManager = SessionManager(this)
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()  // simplest: takes user back to previous activity
        }

        initViews()
        setupClickListeners()
        loadCourses()
        setupTypeSpinner()
    }

    private fun initViews() {
        spinnerCourse = findViewById(R.id.spinnerCourse)
        spinnerType = findViewById(R.id.spinnerType)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnPublish = findViewById(R.id.btnPublish)
        findViewById<ImageView>(R.id.spinnerCourseArrow).setOnClickListener {
            spinnerCourse.performClick()
        }

        findViewById<ImageView>(R.id.spinnerTypeArrow).setOnClickListener {
            spinnerType.performClick()
        }
    }


    private fun setupClickListeners() {
        btnPublish.setOnClickListener {
            publishAnnouncement()
        }
    }

    private fun loadCourses() {
        lifecycleScope.launch {
            try {
                if (NetworkUtils.isOnline(this@PostAnnouncement)) {
                    // Online: fetch from API and cache
                    val apiService = RetrofitClient.getApiService(sessionManager)
                    val response = apiService.getTeacherCourses()
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        courses = response.body()?.data ?: emptyList()
                        cacheCourses(courses)
                        setupCourseSpinner(courses)
                    } else {
                        loadOfflineCourses()
                    }
                } else {
                    loadOfflineCourses()
                }
            } catch (e: Exception) {
                loadOfflineCourses()
            }
        }
    }
    
    private suspend fun cacheCourses(courseList: List<Course>) {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(this@PostAnnouncement)
                val teacherId = sessionManager.getUserId()
                
                val entities = courseList.map { course ->
                    com.example.smd_project.database.entities.TeacherCourseEntity(
                        course_id = course.course_id,
                        teacher_id = teacherId,
                        course_code = course.course_code,
                        course_name = course.course_name,
                        credit_hours = course.credit_hours,
                        semester = course.semester,
                        section = null,
                        enrolled_students = course.enrolled_students ?: 0
                    )
                }
                
                database.teacherCourseDao().deleteByTeacher(teacherId)
                if (entities.isNotEmpty()) {
                    database.teacherCourseDao().insertCourses(entities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun loadOfflineCourses() {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(this@PostAnnouncement)
                val teacherId = sessionManager.getUserId()
                val cachedCourses = database.teacherCourseDao().getCoursesSync(teacherId)
                
                if (cachedCourses.isNotEmpty()) {
                    courses = cachedCourses.map { entity ->
                        Course(
                            course_id = entity.course_id,
                            course_code = entity.course_code,
                            course_name = entity.course_name,
                            credit_hours = entity.credit_hours,
                            semester = entity.semester,
                            description = null,
                            is_required = 0,
                            is_active = 1,
                            instructors = null,
                            schedule = null,
                            enrolled_students = entity.enrolled_students,
                            grade = null,
                            gpa = null,
                            status = null
                        )
                    }
                    
                    withContext(Dispatchers.Main) {
                        setupCourseSpinner(courses)
                        Toast.makeText(this@PostAnnouncement, "Showing cached courses (offline)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@PostAnnouncement, "No cached courses available", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PostAnnouncement, "Error loading offline courses", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupCourseSpinner(courses: List<Course>) {
        val courseNames = courses.map { it.course_name }

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseNames) {

            // Selected item text color (white)
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#FFFFFF"))
                view.textSize = 15f
                view.setPadding(0, 0, 0, 0)
                return view
            }

            // Dropdown items text color (black)
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#000000"))
                view.textSize = 15f
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourse.adapter = adapter
        spinnerCourse.setSelection(0)

        spinnerCourse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCourseId = courses[position].course_id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCourseId = null
            }
        }
    }

    private fun setupTypeSpinner() {
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, announcementTypes) {

            // Selected item text color (white)
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#FFFFFF"))
                view.textSize = 15f
                view.setPadding(0, 0, 0, 0)
                return view
            }

            // Dropdown items text color (black)
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#000000"))
                view.textSize = 15f
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
        spinnerType.setSelection(0)

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedType = announcementTypes[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedType = null
            }
        }
    }

    private fun publishAnnouncement() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty() || selectedCourseId == null || selectedType == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val type = selectedType!!

        val request = PostAnnouncementRequest(
            courseId = selectedCourseId,
            title = title,
            content = content,
            announcement_type = type
        )

        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            try {
                val response = apiService.postAnnouncement(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Log activity
                    val activityManager = ActivityManager(this@PostAnnouncement)
                    val activity = TeacherActivity(
                        activity_type = TeacherActivity.TYPE_ANNOUNCEMENT,
                        title = "Announced: $title",
                        description = "Posted announcement: \"$title\" to ${spinnerCourse.selectedItem}",
                        relatedName = spinnerCourse.selectedItem?.toString()
                    )
                    activityManager.addActivity(activity)

                    Toast.makeText(this@PostAnnouncement, "Announcement posted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@PostAnnouncement,
                        response.body()?.message ?: "Failed to post announcement",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PostAnnouncement, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
