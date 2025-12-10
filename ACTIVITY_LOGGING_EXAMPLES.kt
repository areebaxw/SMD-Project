// Example: How to add activity logging to other teacher actions

// ============================================
// 1. FOR GRADE UPLOAD (UploadGrade.kt)
// ============================================

// Add to imports:
// import com.example.smd_project.models.TeacherActivity
// import com.example.smd_project.utils.ActivityManager

// In the success block of grade upload:
private fun onGradeUploadSuccess(courseName: String, gradesCount: Int) {
    // Log activity
    val activityManager = ActivityManager(this@UploadGrade)
    val activity = TeacherActivity(
        activity_type = TeacherActivity.TYPE_GRADES,
        title = "Grades Uploaded: $courseName",
        description = "Uploaded grades for $gradesCount students"
    )
    activityManager.addActivity(activity)
    
    Toast.makeText(this, "Grades uploaded successfully", Toast.LENGTH_SHORT).show()
}


// ============================================
// 2. FOR SCHEDULE UPDATE (ScheduleActivity.kt)
// ============================================

// Add to imports:
// import com.example.smd_project.models.TeacherActivity
// import com.example.smd_project.utils.ActivityManager

// In the success block of schedule update:
private fun onScheduleUpdateSuccess(courseName: String, day: String, time: String) {
    // Log activity
    val activityManager = ActivityManager(this@ScheduleActivity)
    val activity = TeacherActivity(
        activity_type = TeacherActivity.TYPE_SCHEDULE,
        title = "Schedule Updated",
        description = "Updated $courseName class on $day at $time"
    )
    activityManager.addActivity(activity)
    
    Toast.makeText(this, "Schedule updated successfully", Toast.LENGTH_SHORT).show()
}


// ============================================
// 3. FOR EVALUATION CREATION (EnterMarks.kt)
// ============================================

// In the success block of evaluation creation:
private fun onEvaluationCreated(evaluationTitle: String, totalMarks: Int) {
    // Log activity
    val activityManager = ActivityManager(this@EnterMarks)
    val activity = TeacherActivity(
        activity_type = TeacherActivity.TYPE_MARKS,
        title = "Evaluation Created: $evaluationTitle",
        description = "Created new evaluation with $totalMarks total marks"
    )
    activityManager.addActivity(activity)
    
    Toast.makeText(this, "Evaluation created successfully", Toast.LENGTH_SHORT).show()
}


// ============================================
// 4. FOR COURSE REGISTRATION
// ============================================

// In CourseListActivity or wherever courses are registered:
private fun onCourseRegistered(courseName: String, courseCode: String) {
    // Log activity
    val activityManager = ActivityManager(this@CourseListActivity)
    val activity = TeacherActivity(
        activity_type = "course_assignment", // or add new type
        title = "Course Registered: $courseCode",
        description = "Registered for $courseName ($courseCode)"
    )
    activityManager.addActivity(activity)
    
    Toast.makeText(this, "Course registered successfully", Toast.LENGTH_SHORT).show()
}


// ============================================
// ACCESSING ACTIVITIES ELSEWHERE
// ============================================

// To get all activities of a specific type:
val activityManager = ActivityManager(context)
val announcements = activityManager.getActivitiesByType(TeacherActivity.TYPE_ANNOUNCEMENT)
val attendanceActivities = activityManager.getActivitiesByType(TeacherActivity.TYPE_ATTENDANCE)

// To get all recent activities:
val allActivities = activityManager.getAllActivities()

// To clear activity history:
activityManager.clearAllActivities()


// ============================================
// DISPLAY ACTIVITIES IN OTHER VIEWS
// ============================================

// To show activities in a fragment or another activity:
private fun setupActivityList() {
    val activityManager = ActivityManager(this)
    val activities = activityManager.getRecentActivities(limit = 20)
    
    val adapter = TeacherActivityAdapter(activities)
    recyclerView.apply {
        layoutManager = LinearLayoutManager(this@YourActivity)
        adapter = adapter
    }
}
