package com.example.smd_project

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.AttendanceStudentAdapter
import com.example.smd_project.models.AttendanceItem
import com.example.smd_project.models.Course
import com.example.smd_project.models.MarkAttendanceRequest
import com.example.smd_project.models.Student
import com.example.smd_project.models.TodayAttendanceItem
import com.example.smd_project.models.TeacherActivity
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.repository.TeacherRepository
import com.example.smd_project.repository.AttendanceRecord
import com.example.smd_project.utils.SessionManager
import com.example.smd_project.utils.ActivityManager
import com.example.smd_project.utils.NetworkUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MarkAttendance : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var etDate: EditText
    private lateinit var spinnerCourse: Spinner
    private lateinit var rvStudents: RecyclerView
    private lateinit var btnSubmit: TextView
    private lateinit var btnAllPresent: TextView
    private lateinit var btnAllAbsent: TextView
    private lateinit var tvStudentsHeader: TextView
    private lateinit var tvPresentCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var tvLateCount: TextView
    private lateinit var saveButton: TextView
    private lateinit var calendarIcon: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var backArrow: ImageView
    private lateinit var dropdownArrow: ImageView
    
    private lateinit var attendanceAdapter: AttendanceStudentAdapter
    private val attendanceMap = mutableMapOf<Int, String>() // student_id to status (Present/Absent/Late)
    private val studentNamesMap = mutableMapOf<Int, String>() // student_id to name
    private val studentRollNoMap = mutableMapOf<Int, String>() // student_id to roll_no
    private var selectedCourseId: Int = 0
    private var courses: List<Course> = emptyList()
    private var allStudents: List<Student> = emptyList()
    private lateinit var repository: TeacherRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markattendance)
        
        sessionManager = SessionManager(this)
        repository = TeacherRepository(this)
        
        initViews()
        setupRecyclerView()
        loadCourses()
        setupClickListeners()
        
        // Set today's date
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etDate.setText(today)
    }
    
    private fun initViews() {
        etDate = findViewById(R.id.etDate)
        spinnerCourse = findViewById(R.id.spinnerCourse)
        rvStudents = findViewById(R.id.rvStudents)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnAllPresent = findViewById(R.id.btnAllPresent)
        btnAllAbsent = findViewById(R.id.allAbsentButton)
        tvStudentsHeader = findViewById(R.id.tvStudentsHeader)
        tvPresentCount = findViewById(R.id.presentCount)
        tvAbsentCount = findViewById(R.id.absentCount)
        tvLateCount = findViewById(R.id.lateCount)
        saveButton = findViewById(R.id.saveButton)
        calendarIcon = findViewById(R.id.calendarIcon)
        searchEditText = findViewById(R.id.searchEditText)
        backArrow = findViewById(R.id.backArrow)
        dropdownArrow = findViewById(R.id.dropdownArrow)
        
        // Back arrow click listener
        backArrow.setOnClickListener {
            finish()
        }
        
        // Dropdown arrow click listener - opens course spinner
        dropdownArrow.setOnClickListener {
            spinnerCourse.performClick()
        }
        
        // Save button click listener
        saveButton.setOnClickListener {
            submitAttendance()
        }
        
        // Calendar icon click listener
        calendarIcon.setOnClickListener {
            showDatePicker()
        }
        
        // Search functionality
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                filterStudents(s.toString())
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    
    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceStudentAdapter(emptyList()) { studentId, status ->
            attendanceMap[studentId] = status
            updateAttendanceCounts()
        }
        
        rvStudents.apply {
            layoutManager = LinearLayoutManager(this@MarkAttendance)
            adapter = attendanceAdapter
        }
    }
    
    private fun setupClickListeners() {
        // All Present Button
        btnAllPresent.setOnClickListener {
            attendanceMap.replaceAll { _, _ -> "Present" }
            attendanceAdapter.updateAttendanceStatus(attendanceMap)
            updateAttendanceCounts()
        }
        
        // All Absent Button
        btnAllAbsent.setOnClickListener {
            attendanceMap.replaceAll { _, _ -> "Absent" }
            attendanceAdapter.updateAttendanceStatus(attendanceMap)
            updateAttendanceCounts()
        }
        
        // Course Spinner selection
        spinnerCourse.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < courses.size) {
                    selectedCourseId = courses[position].course_id
                    attendanceMap.clear()
                    loadStudents(selectedCourseId)
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
        
        // Submit button
        btnSubmit.setOnClickListener {
            submitAttendance()
        }
    }
    
    private fun loadCourses() {
        lifecycleScope.launch {
            try {
                if (NetworkUtils.isOnline(this@MarkAttendance)) {
                    // Online: fetch from API and cache
                    val apiService = RetrofitClient.getApiService(sessionManager)
                    val response = apiService.getTeacherCourses()
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        courses = response.body()?.data ?: emptyList()
                        
                        // Cache courses for offline use
                        cacheCourses(courses)
                        
                        setupCoursesSpinner()
                    } else {
                        // Try offline fallback
                        loadOfflineCourses()
                    }
                } else {
                    // Offline: load from cache
                    loadOfflineCourses()
                }
            } catch (e: Exception) {
                // On error, try offline fallback
                loadOfflineCourses()
            }
        }
    }
    
    private suspend fun cacheCourses(courseList: List<Course>) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val database = com.example.smd_project.database.AppDatabase.getDatabase(this@MarkAttendance)
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
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val database = com.example.smd_project.database.AppDatabase.getDatabase(this@MarkAttendance)
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
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        setupCoursesSpinner()
                        Toast.makeText(this@MarkAttendance, "Showing cached courses (offline)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(this@MarkAttendance, "No cached courses available", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(this@MarkAttendance, "Error loading offline courses", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupCoursesSpinner() {
        if (courses.isNotEmpty()) {
            val courseNames = courses.map { it.course_name }
            val adapter = ArrayAdapter(
                this@MarkAttendance,
                R.layout.spinner_item_white,
                courseNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCourse.adapter = adapter
            
            // Load students for first course
            selectedCourseId = courses[0].course_id
            loadStudents(courses[0].course_id)
        } else {
            Toast.makeText(this@MarkAttendance, "No courses found", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadStudents(courseId: Int) {
        // Check network before making API call
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "No internet connection. Cannot load students.", Toast.LENGTH_SHORT).show()
            return
        }
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getCourseStudents(courseId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val students = response.body()?.data ?: emptyList()
                    allStudents = students
                    searchEditText.text.clear()
                    attendanceAdapter.updateStudents(students)
                    
                    // Store student names and roll numbers for offline sync
                    students.forEach { student ->
                        studentNamesMap[student.student_id] = student.full_name
                        studentRollNoMap[student.student_id] = student.roll_no
                    }
                    
                    // Fetch today's attendance records first
                    loadTodayAttendance(courseId, students)
                    
                    // Update header with student count
                    tvStudentsHeader.text = "Students (${students.size})"
                    updateAttendanceCounts()
                } else {
                    Toast.makeText(this@MarkAttendance,
                        "Failed to load students",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MarkAttendance, "Error loading students", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun loadTodayAttendance(courseId: Int, students: List<Student>) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                // Get the selected date from etDate field
                val selectedDate = etDate.text.toString()
                val response = apiService.getTodayAttendance(courseId, selectedDate)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val attendanceRecords = response.body()?.data ?: emptyList()
                    
                    // Clear attendance map
                    attendanceMap.clear()
                    
                    // Populate attendance map with existing records
                    attendanceRecords.forEach { record ->
                        attendanceMap[record.student_id] = record.status
                    }
                    
                    // Initialize remaining students with Absent as default
                    students.forEach { student ->
                        if (!attendanceMap.containsKey(student.student_id)) {
                            attendanceMap[student.student_id] = "Absent"
                        }
                    }
                    
                    // Update adapter with current attendance status
                    attendanceAdapter.updateAttendanceStatus(attendanceMap)
                    updateAttendanceCounts()
                } else {
                    // If no records found, initialize all with Absent
                    attendanceMap.clear()
                    students.forEach { student ->
                        attendanceMap[student.student_id] = "Absent"
                    }
                    updateAttendanceCounts()
                }
            } catch (e: Exception) {
                // On error, initialize all with Absent
                attendanceMap.clear()
                students.forEach { student ->
                    attendanceMap[student.student_id] = "Absent"
                }
                updateAttendanceCounts()
            }
        }
    }
    
    private fun updateAttendanceCounts() {
        val presentCount = attendanceMap.values.count { it == "Present" }
        val absentCount = attendanceMap.values.count { it == "Absent" }
        val lateCount = attendanceMap.values.count { it == "Late" }
        
        tvPresentCount.text = presentCount.toString()
        tvAbsentCount.text = absentCount.toString()
        tvLateCount.text = lateCount.toString()
    }
    
    private fun submitAttendance() {
        if (selectedCourseId == 0) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (attendanceMap.isEmpty()) {
            Toast.makeText(this, "Please load students first", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get the selected date from etDate field
        val selectedDate = etDate.text.toString()
        
        // Create attendance records for repository
        val records = attendanceMap.map { (studentId, status) ->
            AttendanceRecord(
                student_id = studentId,
                student_name = studentNamesMap[studentId],
                roll_no = studentRollNoMap[studentId],
                status = status
            )
        }
        
        lifecycleScope.launch {
            try {
                val result = repository.markAttendance(
                    courseId = selectedCourseId,
                    date = selectedDate,
                    attendanceRecords = records
                )
                
                if (result.isSuccess) {
                    // Log activity
                    val activityManager = ActivityManager(this@MarkAttendance)
                    val presentCount = attendanceMap.values.count { it == "Present" }
                    val activity = TeacherActivity(
                        activity_type = TeacherActivity.TYPE_ATTENDANCE,
                        title = "Attendance Updated",
                        description = "Marked attendance for ${spinnerCourse.selectedItem} - $presentCount present on $selectedDate"
                    )
                    activityManager.addActivity(activity)

                    val message = if (NetworkUtils.isOnline(this@MarkAttendance)) {
                        "Attendance marked successfully for $selectedDate"
                    } else {
                        "Attendance saved locally. Will sync when online."
                    }
                    Toast.makeText(this@MarkAttendance, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@MarkAttendance,
                        "Failed to mark attendance",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MarkAttendance, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                etDate.setText(dateFormat.format(selectedDate.time))
                // Reload attendance when date is changed
                loadTodayAttendance(selectedCourseId, allStudents)
            },
            year,
            month,
            day
        )
        
        datePickerDialog.show()
    }
    
    private fun filterStudents(query: String) {
        if (query.isEmpty()) {
            attendanceAdapter.updateStudents(allStudents)
        } else {
            val filteredStudents = allStudents.filter { student ->
                student.full_name.contains(query, ignoreCase = true) ||
                student.roll_no.contains(query, ignoreCase = true)
            }
            attendanceAdapter.updateStudents(filteredStudents)
            tvStudentsHeader.text = "Students (${filteredStudents.size})"
        }
    }
}

