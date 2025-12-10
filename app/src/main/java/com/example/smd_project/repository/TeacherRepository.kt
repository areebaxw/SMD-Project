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
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TeacherRepository(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val sessionManager = SessionManager(context)
    private val apiService = RetrofitClient.getApiService(sessionManager)
    private val gson = Gson()
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("teacher_offline_cache", Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "TeacherRepository"
        private const val CACHE_DASHBOARD = "cached_teacher_dashboard"
        private const val CACHE_COURSES = "cached_teacher_courses"
        private const val CACHE_ANNOUNCEMENTS = "cached_teacher_announcements"
    }
    
    // ========== Dashboard Data ==========
    suspend fun getDashboardData(forceRefresh: Boolean = false): Result<TeacherDashboard?> {
        return withContext(Dispatchers.IO) {
            try {
                // If offline, return cache immediately
                if (!NetworkUtils.isOnline(context)) {
                    Log.d(TAG, "Device is offline - returning cached teacher dashboard")
                    return@withContext Result.success(getCachedDashboard())
                }
                
                // Online - check if we should fetch fresh data
                if (forceRefresh) {
                    Log.d(TAG, "Force refresh - fetching teacher dashboard from API")
                    try {
                        val response = apiService.getTeacherDashboard()
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            val dashboardData = response.body()?.data
                            
                            // Cache the data
                            dashboardData?.let {
                                cacheDashboard(it)
                                // Also cache to Room for structured data
                                cacheTeacherDataToRoom(it)
                                Log.d(TAG, "Fresh teacher data fetched and cached")
                            }
                            
                            return@withContext Result.success(dashboardData)
                        }
                    } catch (networkError: Exception) {
                        Log.e(TAG, "Network error, falling back to cache", networkError)
                    }
                }
                
                // Return cached data
                val cachedData = getCachedDashboard()
                if (cachedData != null) {
                    Log.d(TAG, "Returning cached teacher dashboard data")
                } else {
                    Log.w(TAG, "No cached teacher data available")
                }
                Result.success(cachedData)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in getDashboardData", e)
                Result.success(getCachedDashboard())
            }
        }
    }
    
    private fun cacheDashboard(dashboard: TeacherDashboard) {
        try {
            val json = gson.toJson(dashboard)
            sharedPreferences.edit().putString(CACHE_DASHBOARD, json).apply()
            Log.d(TAG, "Teacher dashboard cached successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching teacher dashboard", e)
        }
    }
    
    private fun getCachedDashboard(): TeacherDashboard? {
        return try {
            val json = sharedPreferences.getString(CACHE_DASHBOARD, null)
            if (json != null) {
                gson.fromJson(json, TeacherDashboard::class.java)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached teacher dashboard", e)
            null
        }
    }
    
    private suspend fun cacheTeacherDataToRoom(dashboard: TeacherDashboard) {
        try {
            val teacherId = sessionManager.getUserId()
            
            // Cache teacher info
            dashboard.teacher?.let { teacher ->
                database.teacherDao().insertTeacher(
                    TeacherEntity(
                        teacher_id = teacher.teacher_id,
                        employee_id = "",
                        full_name = teacher.full_name,
                        email = teacher.email,
                        profile_picture_url = teacher.profile_picture_url,
                        phone = teacher.phone,
                        department = null,
                        designation = null,
                        specialization = null,
                        is_active = 1,
                        created_at = null
                    )
                )
            }
            
            // Cache courses
            dashboard.courses?.let { courses ->
                val courseEntities = courses.map { course ->
                    TeacherCourseEntity(
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
                database.teacherCourseDao().insertCourses(courseEntities)
            }
            
            // Cache today's schedule
            dashboard.todaySchedule?.let { schedules ->
                val scheduleEntities = schedules.map { schedule ->
                    TeacherScheduleEntity(
                        teacher_id = teacherId,
                        course_id = schedule.course_id,
                        course_code = schedule.course_code,
                        course_name = schedule.course_name,
                        day_of_week = getCurrentDayOfWeek(),
                        start_time = schedule.start_time,
                        end_time = schedule.end_time,
                        room = schedule.room_number
                    )
                }
                database.teacherScheduleDao().deleteByTeacher(teacherId)
                database.teacherScheduleDao().insertSchedules(scheduleEntities)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error caching teacher data to Room", e)
        }
    }
    
    // ========== Courses ==========
    suspend fun getCourses(forceRefresh: Boolean = false): Result<List<Course>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    Log.d(TAG, "Offline - returning cached courses")
                    return@withContext Result.success(getCachedCourses())
                }
                
                if (forceRefresh) {
                    try {
                        val response = apiService.getTeacherCourses()
                        if (response.isSuccessful && response.body()?.success == true) {
                            val courses = response.body()?.data ?: emptyList()
                            cacheCourses(courses)
                            return@withContext Result.success(courses)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Network error fetching courses", e)
                    }
                }
                
                Result.success(getCachedCourses())
            } catch (e: Exception) {
                Log.e(TAG, "Error getting courses", e)
                Result.success(getCachedCourses())
            }
        }
    }
    
    private fun cacheCourses(courses: List<Course>) {
        try {
            val json = gson.toJson(courses)
            sharedPreferences.edit().putString(CACHE_COURSES, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error caching courses", e)
        }
    }
    
    private fun getCachedCourses(): List<Course> {
        return try {
            val json = sharedPreferences.getString(CACHE_COURSES, null)
            if (json != null) {
                val type = object : TypeToken<List<Course>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached courses", e)
            emptyList()
        }
    }
    
    // ========== Announcements ==========
    suspend fun getAnnouncements(forceRefresh: Boolean = false): Result<List<Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    Log.d(TAG, "Offline - returning cached announcements")
                    return@withContext Result.success(getCachedAnnouncements())
                }
                
                if (forceRefresh) {
                    try {
                        val response = apiService.getTeacherAnnouncements()
                        if (response.isSuccessful && response.body()?.success == true) {
                            val announcements = response.body()?.data ?: emptyList()
                            cacheAnnouncements(announcements)
                            return@withContext Result.success(announcements)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Network error fetching announcements", e)
                    }
                }
                
                Result.success(getCachedAnnouncements())
            } catch (e: Exception) {
                Log.e(TAG, "Error getting announcements", e)
                Result.success(getCachedAnnouncements())
            }
        }
    }
    
    private fun cacheAnnouncements(announcements: List<Announcement>) {
        try {
            val json = gson.toJson(announcements)
            sharedPreferences.edit().putString(CACHE_ANNOUNCEMENTS, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error caching announcements", e)
        }
    }
    
    private fun getCachedAnnouncements(): List<Announcement> {
        return try {
            val json = sharedPreferences.getString(CACHE_ANNOUNCEMENTS, null)
            if (json != null) {
                val type = object : TypeToken<List<Announcement>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached announcements", e)
            emptyList()
        }
    }
    
    // ========== Attendance Marking (Offline Support) ==========
    suspend fun markAttendance(
        courseId: Int,
        date: String,
        attendanceRecords: List<AttendanceRecord>
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Save to local database first
                val entities = attendanceRecords.map { record ->
                    TeacherAttendanceRecordEntity(
                        course_id = courseId,
                        student_id = record.student_id,
                        student_name = record.student_name ?: "",
                        student_roll_no = record.roll_no ?: "",
                        attendance_date = date,
                        status = record.status,
                        is_synced = false
                    )
                }
                database.teacherAttendanceRecordDao().insertRecords(entities)
                
                if (NetworkUtils.isOnline(context)) {
                    // Try to sync immediately
                    try {
                        val request = com.example.smd_project.models.MarkAttendanceRequest(
                            courseId = courseId,
                            attendanceRecords = attendanceRecords.map { 
                                AttendanceItem(it.student_id, it.status) 
                            },
                            attendanceDate = date
                        )
                        val response = apiService.markAttendance(request)
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Mark as synced
                            entities.forEach { entity ->
                                database.teacherAttendanceRecordDao().markSynced(
                                    entity.course_id, entity.student_id, entity.attendance_date
                                )
                            }
                            Log.d(TAG, "Attendance synced successfully")
                            return@withContext Result.success(true)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync attendance, queued for later", e)
                    }
                }
                
                // Queue for sync if offline or sync failed
                queueForSync(
                    operationType = "MARK_ATTENDANCE",
                    entityType = "attendance",
                    entityId = courseId,
                    payload = gson.toJson(mapOf(
                        "course_id" to courseId,
                        "date" to date,
                        "attendance" to attendanceRecords
                    ))
                )
                
                Log.d(TAG, "Attendance saved locally, will sync when online")
                Result.success(true)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error marking attendance", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Marks Entry (Offline Support) ==========
    suspend fun saveMarks(
        courseId: Int,
        evaluationTypeId: Int,
        evaluationType: String,
        evaluationNumber: Int,
        title: String,
        totalMarks: Int,
        academicYear: String = "2025",
        semester: String = "Fall",
        marks: List<MarkEntry>
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Save to local database first
                val entities = marks.map { mark ->
                    TeacherMarkRecordEntity(
                        course_id = courseId,
                        student_id = mark.student_id,
                        student_name = mark.student_name ?: "",
                        student_roll_no = mark.roll_no ?: "",
                        evaluation_type_id = evaluationTypeId,
                        evaluation_type = evaluationType,
                        marks_obtained = mark.marks_obtained,
                        total_marks = totalMarks.toDouble(),
                        is_synced = false
                    )
                }
                database.teacherMarkRecordDao().insertRecords(entities)
                
                if (NetworkUtils.isOnline(context)) {
                    // Try to sync immediately
                    try {
                        val request = com.example.smd_project.models.EnterMarksRequest(
                            course_id = courseId,
                            evaluation_type_id = evaluationTypeId,
                            evaluation_number = evaluationNumber,
                            title = title,
                            total_marks = totalMarks,
                            academic_year = academicYear,
                            semester = semester,
                            marks_records = marks.map { 
                                com.example.smd_project.models.MarksRecordItem(it.student_id, it.marks_obtained) 
                            }
                        )
                        val response = apiService.enterMarks(request)
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Mark as synced
                            entities.forEach { entity ->
                                database.teacherMarkRecordDao().markSynced(
                                    entity.course_id, entity.student_id, entity.evaluation_type_id
                                )
                            }
                            Log.d(TAG, "Marks synced successfully")
                            return@withContext Result.success(true)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync marks, queued for later", e)
                    }
                }
                
                // Queue for sync if offline or sync failed
                queueForSync(
                    operationType = "SAVE_MARKS",
                    entityType = "marks",
                    entityId = courseId,
                    payload = gson.toJson(mapOf(
                        "course_id" to courseId,
                        "evaluation_type_id" to evaluationTypeId,
                        "evaluation_number" to evaluationNumber,
                        "title" to title,
                        "total_marks" to totalMarks,
                        "academic_year" to academicYear,
                        "semester" to semester,
                        "marks" to marks
                    ))
                )
                
                Log.d(TAG, "Marks saved locally, will sync when online")
                Result.success(true)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error saving marks", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Post Announcement (Offline Support) ==========
    suspend fun postAnnouncement(
        courseId: Int?,
        title: String,
        content: String
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val teacherId = sessionManager.getUserId()
                val tempId = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt() // Negative temp ID
                
                // Save to local database first
                val entity = TeacherAnnouncementEntity(
                    announcement_id = tempId,
                    teacher_id = teacherId,
                    course_id = courseId,
                    title = title,
                    content = content,
                    created_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                    is_synced = false
                )
                database.teacherAnnouncementDao().insertAnnouncement(entity)
                
                if (NetworkUtils.isOnline(context)) {
                    // Try to sync immediately
                    try {
                        val request = com.example.smd_project.models.PostAnnouncementRequest(
                            courseId = courseId,
                            title = title,
                            content = content,
                            announcement_type = "General"
                        )
                        val response = apiService.postAnnouncement(request)
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            database.teacherAnnouncementDao().markSynced(tempId)
                            Log.d(TAG, "Announcement synced successfully")
                            return@withContext Result.success(true)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync announcement, queued for later", e)
                    }
                }
                
                // Queue for sync if offline or sync failed
                queueForSync(
                    operationType = "POST_ANNOUNCEMENT",
                    entityType = "announcement",
                    entityId = tempId,
                    payload = gson.toJson(mapOf(
                        "course_id" to courseId,
                        "title" to title,
                        "content" to content
                    ))
                )
                
                Log.d(TAG, "Announcement saved locally, will sync when online")
                Result.success(true)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error posting announcement", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Sync Queue Management ==========
    private suspend fun queueForSync(
        operationType: String,
        entityType: String,
        entityId: Int?,
        payload: String
    ) {
        val pendingSync = PendingSyncEntity(
            operation_type = operationType,
            entity_type = entityType,
            entity_id = entityId,
            payload = payload
        )
        database.pendingSyncDao().insert(pendingSync)
        Log.d(TAG, "Queued $operationType for sync")
    }
    
    suspend fun syncPendingChanges(): Result<Int> {
        return withContext(Dispatchers.IO) {
            if (!NetworkUtils.isOnline(context)) {
                return@withContext Result.success(0)
            }
            
            var syncedCount = 0
            val pendingItems = database.pendingSyncDao().getAllPending()
            
            for (item in pendingItems) {
                try {
                    val success = when (item.operation_type) {
                        "MARK_ATTENDANCE" -> syncAttendance(item)
                        "SAVE_MARKS" -> syncMarks(item)
                        "POST_ANNOUNCEMENT" -> syncAnnouncement(item)
                        else -> false
                    }
                    
                    if (success) {
                        database.pendingSyncDao().deleteById(item.id)
                        syncedCount++
                        Log.d(TAG, "Synced ${item.operation_type} successfully")
                    } else {
                        database.pendingSyncDao().incrementRetry(item.id)
                        Log.w(TAG, "Failed to sync ${item.operation_type}, will retry later")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing ${item.operation_type}", e)
                    database.pendingSyncDao().incrementRetry(item.id)
                }
            }
            
            Result.success(syncedCount)
        }
    }
    
    private suspend fun syncAttendance(item: PendingSyncEntity): Boolean {
        return try {
            val data = gson.fromJson(item.payload, Map::class.java)
            val courseId = (data["course_id"] as Double).toInt()
            val date = data["date"] as String
            val attendanceJson = gson.toJson(data["attendance"])
            val type = object : TypeToken<List<AttendanceRecord>>() {}.type
            val attendance: List<AttendanceRecord> = gson.fromJson(attendanceJson, type)
            
            val request = com.example.smd_project.models.MarkAttendanceRequest(
                courseId = courseId,
                attendanceRecords = attendance.map { 
                    com.example.smd_project.models.AttendanceItem(it.student_id, it.status) 
                },
                attendanceDate = date
            )
            val response = apiService.markAttendance(request)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing attendance", e)
            false
        }
    }
    
    private suspend fun syncMarks(item: PendingSyncEntity): Boolean {
        return try {
            val data = gson.fromJson(item.payload, Map::class.java)
            val courseId = (data["course_id"] as Double).toInt()
            val evalTypeId = (data["evaluation_type_id"] as Double).toInt()
            val evalNumber = (data["evaluation_number"] as Double).toInt()
            val title = data["title"] as String
            val totalMarks = (data["total_marks"] as Double).toInt()
            val academicYear = data["academic_year"] as String
            val semester = data["semester"] as String
            val marksJson = gson.toJson(data["marks"])
            val type = object : TypeToken<List<MarkEntry>>() {}.type
            val marks: List<MarkEntry> = gson.fromJson(marksJson, type)
            
            val request = com.example.smd_project.models.EnterMarksRequest(
                course_id = courseId,
                evaluation_type_id = evalTypeId,
                evaluation_number = evalNumber,
                title = title,
                total_marks = totalMarks,
                academic_year = academicYear,
                semester = semester,
                marks_records = marks.map { 
                    com.example.smd_project.models.MarksRecordItem(it.student_id, it.marks_obtained) 
                }
            )
            val response = apiService.enterMarks(request)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing marks", e)
            false
        }
    }
    
    private suspend fun syncAnnouncement(item: PendingSyncEntity): Boolean {
        return try {
            val data = gson.fromJson(item.payload, Map::class.java)
            val courseId = (data["course_id"] as? Double)?.toInt()
            val title = data["title"] as String
            val content = data["content"] as String
            
            val request = com.example.smd_project.models.PostAnnouncementRequest(
                courseId = courseId,
                title = title,
                content = content,
                announcement_type = "General"
            )
            val response = apiService.postAnnouncement(request)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing announcement", e)
            false
        }
    }
    
    // ========== Utility ==========
    fun getPendingSyncCount(): LiveData<Int> {
        return database.pendingSyncDao().getPendingCount()
    }
    
    suspend fun getPendingSyncCountSync(): Int {
        return database.pendingSyncDao().getPendingCountSync()
    }
    
    private fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Monday"
        }
    }
}

// Internal data classes for repository use (not API requests)
data class AttendanceRecord(
    val student_id: Int,
    val student_name: String?,
    val roll_no: String?,
    val status: String
)

data class MarkEntry(
    val student_id: Int,
    val student_name: String?,
    val roll_no: String?,
    val marks_obtained: Double,
    val total_marks: Double = 0.0
)
