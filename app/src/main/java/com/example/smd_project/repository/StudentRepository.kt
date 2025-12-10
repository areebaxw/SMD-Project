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
    
    private fun cacheDashboard(dashboard: StudentDashboard) {
        try {
            val json = gson.toJson(dashboard)
            sharedPreferences.edit().putString(CACHE_DASHBOARD, json).apply()
            Log.d(TAG, "Dashboard cached successfully")
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
