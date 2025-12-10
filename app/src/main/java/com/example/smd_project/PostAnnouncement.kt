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
import com.example.smd_project.models.PostAnnouncementRequest
import com.example.smd_project.models.Course
import com.example.smd_project.models.TeacherActivity
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import com.example.smd_project.utils.ActivityManager
import kotlinx.coroutines.launch

class PostAnnouncement : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var spinnerCourse: Spinner
    private lateinit var spinnerType: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnPublish: TextView

    private var selectedCourseId: Int? = null
    private var selectedType: String? = null

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
        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            try {
                val response = apiService.getTeacherCourses()
                if (response.isSuccessful && response.body()?.success == true) {
                    val courses = response.body()?.data ?: emptyList()
                    setupCourseSpinner(courses)
                } else {
                    Toast.makeText(this@PostAnnouncement, "Failed to load courses", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PostAnnouncement, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun setupCourseSpinner(courses: List<Course>) {
        val courseNames = courses.map { it.course_name }

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseNames) {

            // Selected item text color (dark gray - for visibility on white background)
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#333333"))
                view.textSize = 15f
                return view
            }

            // Dropdown items text color (black)
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#333333"))
                view.textSize = 15f
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourse.adapter = adapter

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

            // Selected item text color (dark gray - for visibility on white background)
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#333333"))
                view.textSize = 15f
                return view
            }

            // Dropdown items text color (dark gray)
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#333333"))
                view.textSize = 15f
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

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
