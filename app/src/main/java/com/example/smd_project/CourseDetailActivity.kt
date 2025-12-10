package com.example.smd_project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.R
import com.example.smd_project.models.CourseDetails
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch


class CourseDetailActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private lateinit var tvCourseName: TextView
    private lateinit var tvCourseCode: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCreditHours: TextView
    private lateinit var tvSemester: TextView
    private lateinit var instructorContainer: LinearLayout

    private var courseId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_details2)

        sessionManager = SessionManager(this)

        tvCourseName = findViewById(R.id.course_title)
        tvCourseCode = findViewById(R.id.course_code)
        tvDescription = findViewById(R.id.course_description)
        tvCreditHours = findViewById(R.id.tvCreditHours)
        tvSemester = findViewById(R.id.tvSemester)
        instructorContainer = findViewById(R.id.instructor_container)

        courseId = intent.getIntExtra("course_id", 0)
        if (courseId == 0) {
            Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup toolbar back button
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        loadCourseDetails()
    }


    private fun loadCourseDetails() {
        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            try {
                val response = apiService.getCourseDetails(courseId)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val course: CourseDetails? = apiResponse?.data
                    if (course != null) updateUI(course)
                    else Toast.makeText(
                        this@CourseDetailActivity,
                        "Course not found",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@CourseDetailActivity,
                        "Failed to fetch course",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("CourseDetailActivity", "Response not successful: ${response.code()} ${response.errorBody()?.string()}")
                }




            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CourseDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(course: CourseDetails) {
        tvCourseName.text = course.course_name
        tvCourseCode.text = "Course code: ${course.course_code}"
        tvDescription.text = course.description ?: "N/A"
        tvCreditHours.text = course.credit_hours.toString()
        tvSemester.text = course.semester.toString()

        // Dynamically add instructors
        instructorContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        course.instructors.forEach { instructor ->
            val instructorView = inflater.inflate(R.layout.item_instructor_card, instructorContainer, false)
            val tvInstructorName = instructorView.findViewById<TextView>(R.id.tvInstructorName)
            tvInstructorName.text = instructor.full_name
            instructorContainer.addView(instructorView)
        }
    }
}
