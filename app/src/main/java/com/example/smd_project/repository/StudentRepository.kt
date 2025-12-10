package com.example.smd_project.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.smd_project.database.AppDatabase
import com.example.smd_project.database.entities.*
import com.example.smd_project.models.*
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentRepository(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val sessionManager = SessionManager(context)
    private val apiService = RetrofitClient.getApiService(sessionManager)
    private val gson = Gson()
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("offline_cache", Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "StudentRepository"
        private const val CACHE_DASHBOARD = "cached_dashboard"
    }
    
    // ========== Dashboard Data ==========
    suspend fun getDashboardData(forceRefresh: Boolean = false): Result<StudentDashboard?> {
        return withContext(Dispatchers.IO) {
            try {
                // If offline, return cache immediately - don't even try network
                if (!NetworkUtils.isOnline(context)) {
                    Log.d(TAG, "Device is offline - returning cached data")
                    return@withContext Result.success(getCachedDashboard())
                }
                
                // Online - check if we should fetch fresh data
                if (forceRefresh) {
                    Log.d(TAG, "Force refresh - fetching from API")
                    try {
                        val response = apiService.getStudentDashboard()
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            val dashboardData = response.body()?.data
                            
                            // Cache the data
                            dashboardData?.let {
                                cacheDashboard(it)
                                Log.d(TAG, "Fresh data fetched and cached")
                            }
                            
                            return@withContext Result.success(dashboardData)
                        }
                    } catch (networkError: Exception) {
                        Log.e(TAG, "Network error, falling back to cache", networkError)
                    }
                }
                
                // Return cached data (not forcing refresh, or fetch failed)
                val cachedData = getCachedDashboard()
                if (cachedData != null) {
                    Log.d(TAG, "Returning cached dashboard data")
                } else {
                    Log.w(TAG, "No cached data available")
                }
                Result.success(cachedData)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in getDashboardData", e)
                // Always try to return cached data on any error
                Result.success(getCachedDashboard())
            }
        }
    }
    
    private suspend fun cacheDashboard(dashboard: StudentDashboard) {
        try {
            // Save to SharedPreferences for quick access
            val json = gson.toJson(dashboard)
            sharedPreferences.edit().putString(CACHE_DASHBOARD, json).apply()
            Log.d(TAG, "Dashboard cached to SharedPreferences")
            
            // Also save announcements to Room database for offline access
            if (dashboard.announcements.isNotEmpty()) {
                val announcementEntities = dashboard.announcements.map { announcement ->
                    AnnouncementEntity(
                        announcement_id = announcement.announcement_id,
                        teacher_id = announcement.teacher_id,
                        course_id = announcement.course_id,
                        title = announcement.title,
                        content = announcement.content,
                        announcement_type = announcement.announcement_type,
                        is_active = announcement.is_active,
                        created_at = announcement.created_at,
                        updated_at = announcement.updated_at ?: "",
                        teacher_name = announcement.teacher_name,
                        course_name = announcement.course_name,
                        last_synced_at = System.currentTimeMillis()
                    )
                }
                database.announcementDao().insertAnnouncements(announcementEntities)
                Log.d(TAG, "Saved ${announcementEntities.size} announcements to database")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error caching dashboard", e)
        }
    }
    
    private fun getCachedDashboard(): StudentDashboard? {
        return try {
            val json = sharedPreferences.getString(CACHE_DASHBOARD, null)
            if (json != null) {
                gson.fromJson(json, StudentDashboard::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached dashboard", e)
            null
        }
    }
    
    // ========== Courses ==========
    fun getCourses(): LiveData<List<EnrollmentEntity>> {
        val studentId = sessionManager.getUserId()
        return database.enrollmentDao().getEnrollmentsByStudent(studentId)
    }
    
    suspend fun refreshCourses(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    return@withContext Result.success(false)
                }
                
                val response = apiService.getStudentCourses()
                if (response.isSuccessful && response.body()?.success == true) {
                    val courses = response.body()?.data ?: emptyList()
                    val studentId = sessionManager.getUserId()
                    
                    // Convert courses to enrollment entities
                    val enrollments = courses.map { course ->
                        EnrollmentEntity(
                            enrollment_id = course.course_id * 1000 + studentId, // Synthetic ID
                            student_id = studentId,
                            course_id = course.course_id,
                            academic_year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString(),
                            semester = course.semester?.toString() ?: "1",
                            enrollment_date = "",
                            status = course.status ?: "Enrolled",
                            grade = course.grade,
                            gpa = course.gpa,
                            created_at = "",
                            updated_at = "",
                            course_code = course.course_code,
                            course_name = course.course_name,
                            description = course.description,
                            credit_hours = course.credit_hours,
                            teacher_name = course.instructors,
                            teacher_id = null,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                    
                    // Clear old enrollments and save fresh data from API
                    // This ensures unenrolled courses are removed
                    database.enrollmentDao().clearAll()
                    database.enrollmentDao().insertEnrollments(enrollments)
                    Log.d(TAG, "Refreshed enrollments: cleared old and saved ${enrollments.size} courses to database")
                    
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to fetch courses"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing courses", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Marks ==========
    fun getMarks(): LiveData<List<MarkEntity>> {
        val studentId = sessionManager.getUserId()
        return database.markDao().getMarksByStudent(studentId)
    }
    
    suspend fun refreshMarks(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    return@withContext Result.success(false)
                }
                
                val response = apiService.getStudentMarks()
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to fetch marks"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing marks", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Attendance ==========
    fun getAttendance(): LiveData<List<AttendanceEntity>> {
        val studentId = sessionManager.getUserId()
        return database.attendanceDao().getAttendanceByStudent(studentId)
    }
    
    suspend fun refreshAttendance(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    return@withContext Result.success(false)
                }
                
                val response = apiService.getStudentAttendance()
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "Attendance data refreshed from API")
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to fetch attendance"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing attendance", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Announcements ==========
    fun getAnnouncements(limit: Int = 50): LiveData<List<AnnouncementEntity>> {
        return database.announcementDao().getAnnouncements(limit)
    }
    
    suspend fun refreshAnnouncements(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    return@withContext Result.success(false)
                }
                
                // Announcements are typically included in dashboard
                getDashboardData(forceRefresh = true)
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing announcements", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Notifications ==========
    fun getNotifications(limit: Int = 100): LiveData<List<NotificationEntity>> {
        return database.notificationDao().getNotifications(limit)
    }
    
    fun getUnreadNotificationCount(): LiveData<Int> {
        return database.notificationDao().getUnreadCount()
    }
    
    suspend fun refreshNotifications(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    return@withContext Result.success(false)
                }
                
                val response = apiService.getStudentNotifications(100)
                if (response.isSuccessful && response.body()?.success == true) {
                    val notifications = response.body()?.data ?: emptyList()
                    
                    val entities = notifications.map { notification ->
                        NotificationEntity(
                            notification_id = notification.notification_id,
                            recipient_type = notification.recipient_type,
                            recipient_id = notification.recipient_id,
                            title = notification.title,
                            message = notification.message,
                            notification_type = notification.notification_type,
                            reference_id = null,
                            is_read = notification.is_read,
                            created_at = notification.created_at
                        )
                    }
                    database.notificationDao().insertNotifications(entities)
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to fetch notifications"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing notifications", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun markNotificationAsRead(notificationId: Int): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Update locally first
                database.notificationDao().markAsRead(notificationId)
                
                // Sync with server if online
                if (NetworkUtils.isOnline(context)) {
                    val response = apiService.markStudentNotificationAsRead(notificationId)
                    if (response.isSuccessful) {
                        Result.success(true)
                    } else {
                        // Keep local change even if server fails
                        Result.success(true)
                    }
                } else {
                    // Will sync when online
                    Result.success(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Fees ==========
    fun getFees(): LiveData<List<StudentFeeEntity>> {
        val studentId = sessionManager.getUserId()
        return database.studentFeeDao().getFeesByStudent(studentId)
    }
    
    suspend fun refreshFees(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    return@withContext Result.success(false)
                }
                
                val response = apiService.getStudentFees()
                if (response.isSuccessful && response.body()?.success == true) {
                    val fees = response.body()?.data ?: emptyList()
                    val studentId = sessionManager.getUserId()
                    
                    // Convert fee items to entities
                    val feeEntities = fees.map { fee ->
                        StudentFeeEntity(
                            fee_id = fee.fee_id,
                            student_id = fee.student_id,
                            fee_structure_id = fee.fee_structure_id,
                            total_amount = fee.total_amount,
                            paid_amount = fee.paid_amount ?: 0.0,
                            remaining_amount = fee.remaining_amount,
                            payment_status = fee.payment_status,
                            due_date = fee.due_date,
                            created_at = "",
                            updated_at = "",
                            program = fee.program,
                            semester = fee.semester,
                            academic_year = fee.academic_year,
                            structure_total_fee = fee.total_amount,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                    
                    // Save to database
                    database.studentFeeDao().insertFees(feeEntities)
                    Log.d(TAG, "Saved ${feeEntities.size} fees to database")
                    
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to fetch fees"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing fees", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Clear Cache ==========
    suspend fun clearAllCache() {
        withContext(Dispatchers.IO) {
            database.studentDao().clearAll()
            database.courseDao().clearAll()
            database.announcementDao().clearAll()
            database.attendanceDao().clearAll()
            database.markDao().clearAll()
            database.evaluationDao().clearAll()
            database.notificationDao().clearAll()
            database.classScheduleDao().clearAll()
            database.enrollmentDao().clearAll()
            database.studentFeeDao().clearAll()
        }
    }
}
