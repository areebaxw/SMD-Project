package com.example.smd_project.activities

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.R
import com.example.smd_project.models.Course
import com.example.smd_project.models.EnrollCoursesRequest
import com.example.smd_project.models.EnrollCourseRequest
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class CourseRegistrationActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "CourseRegistration"
    }
    
    private lateinit var progressBar: ProgressBar
    private lateinit var coursesContainer: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var selectedCoursesText: TextView
    private lateinit var totalCreditsText: TextView
    private lateinit var btnRegister: AppCompatButton
    private lateinit var menuIcon: ImageView
    private lateinit var sessionManager: SessionManager

    private val selectedCourses = mutableSetOf<Int>()
    private val courseCreditsMap = mutableMapOf<Int, Int>()
    private val courseStatusMap = mutableMapOf<Int, String>() // track enroll/drop status
    private lateinit var allCourses: List<Course>
    private lateinit var registeredCourseIds: Set<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_restigration)

        sessionManager = SessionManager(this)
        initializeViews()
        setupListeners()
        loadAvailableCourses()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progress_bar)
        coursesContainer = findViewById(R.id.courses_container)
        searchEditText = findViewById(R.id.search_edit_text)
        selectedCoursesText = findViewById(R.id.selected_courses_text)
        totalCreditsText = findViewById(R.id.total_credits_text)
        btnRegister = findViewById(R.id.btnSave)
        menuIcon = findViewById(R.id.menu_icon)
        
        // Make back arrow white
        menuIcon.setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun setupListeners() {
        menuIcon.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            if (selectedCourses.isEmpty()) {
                Toast.makeText(this, "Please select at least one course", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            registerSelectedCourses()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCourses(s.toString())
            }
        })
    }

    private fun loadAvailableCourses() {
        progressBar.visibility = ProgressBar.VISIBLE
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting to load available and registered courses...")
                
                // First get registered courses to show which ones are already enrolled
                val registeredResponse = RetrofitClient.getApiService(sessionManager).getRegisteredCourses()
                Log.d(TAG, "Registered courses response - Code: ${registeredResponse.code()}, isSuccessful: ${registeredResponse.isSuccessful}")
                
                registeredCourseIds = if (registeredResponse.isSuccessful) {
                    try {
                        if (registeredResponse.body()?.success == true) {
                            val ids = registeredResponse.body()?.data?.map { it.course_id }?.toSet() ?: emptySet()
                            Log.d(TAG, "Registered course IDs: $ids")
                            ids
                        } else {
                            Log.w(TAG, "Success flag false: ${registeredResponse.body()?.message}")
                            emptySet()
                        }
                    } catch (parseError: Exception) {
                        Log.e(TAG, "Failed to parse registered courses", parseError)
                        emptySet()
                    }
                } else {
                    Log.w(TAG, "Failed to load registered courses: ${registeredResponse.code()}")
                    emptySet()
                }

                // Then get available courses
                val availableResponse = RetrofitClient.getApiService(sessionManager).getAvailableCourses()
                Log.d(TAG, "Available courses response - Code: ${availableResponse.code()}, isSuccessful: ${availableResponse.isSuccessful}")
                
                if (availableResponse.isSuccessful) {
                    try {
                        if (availableResponse.body()?.success == true) {
                            allCourses = availableResponse.body()?.data ?: emptyList()
                            Log.d(TAG, "Loaded ${allCourses.size} available courses")
                            displayCourses(allCourses)
                        } else {
                            Log.e(TAG, "Success flag false: ${availableResponse.body()?.message}")
                            Toast.makeText(this@CourseRegistrationActivity, "Failed to load courses", Toast.LENGTH_SHORT).show()
                        }
                    } catch (parseError: Exception) {
                        Log.e(TAG, "Failed to parse available courses", parseError)
                        Toast.makeText(this@CourseRegistrationActivity, "Error parsing courses: ${parseError.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to load available courses: ${availableResponse.code()}")
                    Toast.makeText(this@CourseRegistrationActivity, "Server error: ${availableResponse.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadAvailableCourses", e)
                Toast.makeText(this@CourseRegistrationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun displayCourses(courses: List<Course>) {
        coursesContainer.removeAllViews()

        if (courses.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No courses available"
                textSize = 16f
                setTextColor(android.graphics.Color.GRAY)
            }
            coursesContainer.addView(emptyText)
            return
        }

        for (course in courses) {
            val courseCard = createCourseCard(course)
            coursesContainer.addView(courseCard)
        }
    }

    private fun createCourseCard(course: Course): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.WHITE)
            elevation = 2f
            
            // Create rounded corners (Apple style)
            val drawable = android.graphics.drawable.GradientDrawable()
            drawable.setColor(android.graphics.Color.WHITE)
            drawable.cornerRadius = 12f // Rounded corners
            background = drawable
        }

        // Top row: Course code and status badge
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val courseCodeText = TextView(this).apply {
            text = course.course_code
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        topRow.addView(courseCodeText)

        val statusBadge = TextView(this).apply {
            val isRequired = course.is_required == 1  // Convert TINYINT to boolean
            val isRegistered = registeredCourseIds.contains(course.course_id)
            text = when {
                isRegistered -> "Registered"
                isRequired -> "Required"
                else -> "Elective"
            }
            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setPadding(12, 6, 12, 6)
            
            // Rounded badge background
            val badgeDrawable = android.graphics.drawable.GradientDrawable()
            badgeDrawable.setColor(when {
                isRegistered -> android.graphics.Color.parseColor("#34C759") // Apple green
                isRequired -> android.graphics.Color.parseColor("#007AFF") // Apple blue
                else -> android.graphics.Color.parseColor("#FF9500") // Apple orange
            })
            badgeDrawable.cornerRadius = 6f
            background = badgeDrawable
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        topRow.addView(statusBadge)
        card.addView(topRow)

        // Course name
        val courseNameText = TextView(this).apply {
            text = course.course_name
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 0)
            }
        }
        card.addView(courseNameText)

        // Credits
        val creditsText = TextView(this).apply {
            text = "Credits: ${course.credit_hours}"
            textSize = 13f
            setTextColor(android.graphics.Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }
        card.addView(creditsText)

        // Bottom row: Enroll button + Checkbox
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 12, 0, 0)
            }
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val checkbox = android.widget.CheckBox(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isChecked = selectedCourses.contains(course.course_id)
            isEnabled = !registeredCourseIds.contains(course.course_id)

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCourses.add(course.course_id)
                    courseCreditsMap[course.course_id] = course.credit_hours
                } else {
                    selectedCourses.remove(course.course_id)
                    courseCreditsMap.remove(course.course_id)
                }
                updateStats()
            }
        }
        bottomRow.addView(checkbox)

        val enrollBtn = AppCompatButton(this).apply {
            val isRegistered = registeredCourseIds.contains(course.course_id)
            text = if (isRegistered) "Drop" else "Enroll"
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(24, 10, 24, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(12, 0, 0, 0)
            }
            
            // Rounded button background
            val btnDrawable = android.graphics.drawable.GradientDrawable()
            btnDrawable.setColor(if (isRegistered)
                android.graphics.Color.parseColor("#FF3B30") // Apple red
            else
                android.graphics.Color.parseColor("#34C759") // Apple green
            )
            btnDrawable.cornerRadius = 8f
            background = btnDrawable
            setTextColor(android.graphics.Color.WHITE)

            setOnClickListener {
                if (registeredCourseIds.contains(course.course_id)) {
                    // Drop course
                    dropCourse(course)
                } else {
                    // Quick enroll single course
                    enrollSingleCourse(course)
                }
            }
        }
        bottomRow.addView(enrollBtn)

        val spacer = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        }
        bottomRow.addView(spacer)

        card.addView(bottomRow)

        return card
    }

    private fun filterCourses(query: String) {
        val filtered = if (query.isEmpty()) {
            allCourses
        } else {
            allCourses.filter {
                it.course_code.contains(query, ignoreCase = true) ||
                        it.course_name.contains(query, ignoreCase = true)
            }
        }
        displayCourses(filtered)
    }

    private fun updateStats() {
        selectedCoursesText.text = "Selected: ${selectedCourses.size} courses"
        val totalCredits = selectedCourses.sumOf { courseCreditsMap[it] ?: 0 }
        totalCreditsText.text = "Total Credits: $totalCredits"
    }

    private fun registerSelectedCourses() {
        if (selectedCourses.isEmpty()) return

        progressBar.visibility = ProgressBar.VISIBLE
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Registering ${selectedCourses.size} courses: $selectedCourses")
                val request = EnrollCoursesRequest(selectedCourses.toList())
                val response = RetrofitClient.getApiService(sessionManager).enrollInCourses(request)

                Log.d(TAG, "Enrollment response - Code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "Enrollment successful")
                    Toast.makeText(
                        this@CourseRegistrationActivity,
                        "Courses registered successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                    selectedCourses.clear()
                    courseCreditsMap.clear()
                    updateStats()
                    loadAvailableCourses() // Reload to update registered status
                } else {
                    Log.e(TAG, "Enrollment failed: ${response.body()?.message}")
                    Toast.makeText(
                        this@CourseRegistrationActivity,
                        "Registration failed: ${response.body()?.message ?: "Unknown error"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in registerSelectedCourses", e)
                Toast.makeText(this@CourseRegistrationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun enrollSingleCourse(course: Course) {
        progressBar.visibility = ProgressBar.VISIBLE
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Enrolling in course: ${course.course_id} (${course.course_code})")
                val request = EnrollCourseRequest(course.course_id)
                val response = RetrofitClient.getApiService(sessionManager).enrollInCourse(request)

                Log.d(TAG, "Single enroll response - Code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "Single enrollment successful")
                    Toast.makeText(
                        this@CourseRegistrationActivity,
                        "Enrolled in ${course.course_code} successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    registeredCourseIds = registeredCourseIds.plus(course.course_id)
                    loadAvailableCourses() // Reload to update UI
                } else {
                    Log.e(TAG, "Single enrollment failed: ${response.body()?.message}")
                    Toast.makeText(
                        this@CourseRegistrationActivity,
                        "Enrollment failed: ${response.body()?.message ?: "Unknown error"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in enrollSingleCourse", e)
                Toast.makeText(this@CourseRegistrationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    private fun dropCourse(course: Course) {
        progressBar.visibility = ProgressBar.VISIBLE
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Dropping course: ${course.course_id} (${course.course_code})")
                val response = RetrofitClient.getApiService(sessionManager).dropCourse(course.course_id)

                Log.d(TAG, "Drop course response - Code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "Drop successful")
                    Toast.makeText(
                        this@CourseRegistrationActivity,
                        "Dropped ${course.course_code} successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    registeredCourseIds = registeredCourseIds.minus(course.course_id)
                    loadAvailableCourses() // Reload to update UI
                } else {
                    Log.e(TAG, "Drop failed: ${response.body()?.message}")
                    Toast.makeText(
                        this@CourseRegistrationActivity,
                        "Drop failed: ${response.body()?.message ?: "Unknown error"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in dropCourse", e)
                Toast.makeText(this@CourseRegistrationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }
}
