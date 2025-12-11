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
                // Get client's current day of week for correct class schedule
                val dayOfWeek = java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH)
                    .format(java.util.Date())
                Log.d(TAG, "Client day of week: $dayOfWeek")
                
                // If offline, return cache immediately - don't even try network
                if (!NetworkUtils.isOnline(context)) {
                    Log.d(TAG, "Device is offline - returning cached data")
                    return@withContext Result.success(getCachedDashboard())
                }
                
                // Online - always fetch fresh data from server (simplest approach)
                Log.d(TAG, "Fetching from API with day: $dayOfWeek")
                try {
                    val response = apiService.getStudentDashboard(dayOfWeek)
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val dashboardData = response.body()?.data
                        
                        // Cache the data for offline use
                        dashboardData?.let {
                            cacheDashboard(it)
                            Log.d(TAG, "Fresh data fetched and cached, today_classes: ${it.today_classes.size}")
                        }
                        
                        return@withContext Result.success(dashboardData)
                    } else {
                        Log.e(TAG, "API returned error: ${response.code()} ${response.message()}")
                    }
                } catch (networkError: Exception) {
                    Log.e(TAG, "Network error, falling back to cache", networkError)
                }
                
                // Fetch failed - return cached data as fallback
                val cachedData = getCachedDashboard()
                if (cachedData != null) {
                    Log.d(TAG, "Returning cached dashboard data as fallback")
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
    
    fun getAttendanceSummary(): LiveData<List<AttendanceSummaryEntity>> {
        val studentId = sessionManager.getUserId()
        return database.attendanceSummaryDao().getSummaryByStudent(studentId)
    }
    
    suspend fun getAttendanceSummaryOffline(): List<AttendanceSummaryEntity> {
        return withContext(Dispatchers.IO) {
            val studentId = sessionManager.getUserId()
            database.attendanceSummaryDao().getSummaryByStudentSync(studentId)
        }
    }
    
    suspend fun refreshAttendance(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    return@withContext Result.success(false)
                }
                
                val response = apiService.getStudentAttendance()
                if (response.isSuccessful && response.body()?.success == true) {
                    val attendanceList = response.body()?.data ?: emptyList()
                    val studentId = sessionManager.getUserId()
                    
                    // Convert to AttendanceSummaryEntity and save to Room
                    val summaryEntities = attendanceList.map { summary ->
                        AttendanceSummaryEntity(
                            id = summary.course_id * 10000 + studentId, // Unique ID per student-course
                            student_id = studentId,
                            course_id = summary.course_id,
                            course_name = summary.course_name,
                            course_code = summary.course_code,
                            present_count = summary.present,
                            absent_count = summary.absent,
                            late_count = summary.late,
                            excused_count = summary.excused,
                            total_count = summary.total,
                            percentage = summary.percentage,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                    
                    if (summaryEntities.isNotEmpty()) {
                        // Clear old summaries for this student and insert new ones
                        database.attendanceSummaryDao().clearByStudent(studentId)
                        database.attendanceSummaryDao().insertAll(summaryEntities)
                        Log.d(TAG, "Saved ${summaryEntities.size} attendance summaries to database")
                    }
                    
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
                    // Use fee_total_amount (from student_fees table) if available, otherwise fall back to total_amount
                    val feeEntities = fees.map { fee ->
                        val actualTotalAmount = fee.fee_total_amount ?: fee.total_amount
                        StudentFeeEntity(
                            fee_id = fee.fee_id,
                            student_id = fee.student_id,
                            fee_structure_id = fee.fee_structure_id,
                            total_amount = actualTotalAmount,
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
    
    // ========== Payment History ==========
    fun getPaymentHistory(feeId: Int): LiveData<List<PaymentHistoryEntity>> {
        return database.paymentHistoryDao().getPaymentHistoryByFee(feeId)
    }
    
    fun getPaymentHistoryByStudent(): LiveData<List<PaymentHistoryEntity>> {
        val studentId = sessionManager.getUserId()
        return database.paymentHistoryDao().getPaymentHistoryByStudent(studentId)
    }
    
    suspend fun getPaymentHistoryOffline(feeId: Int): List<PaymentHistoryEntity> {
        return withContext(Dispatchers.IO) {
            database.paymentHistoryDao().getPaymentHistoryByFeeSync(feeId)
        }
    }
    
    suspend fun refreshPaymentHistory(feeId: Int): Result<List<PaymentHistoryItem>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    // Return offline data if available
                    val cachedPayments = database.paymentHistoryDao().getPaymentHistoryByFeeSync(feeId)
                    if (cachedPayments.isNotEmpty()) {
                        val items = cachedPayments.map { entity ->
                            PaymentHistoryItem(
                                payment_id = entity.payment_id,
                                student_id = entity.student_id,
                                fee_id = entity.fee_id,
                                amount_paid = entity.amount_paid,
                                payment_method = entity.payment_method,
                                remarks = entity.remarks,
                                created_at = entity.created_at
                            )
                        }
                        return@withContext Result.success(items)
                    }
                    return@withContext Result.failure(Exception("No internet connection and no cached data"))
                }
                
                val response = apiService.getFeePaymentHistory(feeId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val payments = response.body()?.data ?: emptyList()
                    
                    // Save to database for offline use
                    val entities = payments.map { payment ->
                        PaymentHistoryEntity(
                            payment_id = payment.payment_id,
                            student_id = payment.student_id,
                            fee_id = payment.fee_id,
                            amount_paid = payment.amount_paid,
                            payment_method = payment.payment_method,
                            remarks = payment.remarks,
                            created_at = payment.created_at,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                    
                    if (entities.isNotEmpty()) {
                        // Clear old payments for this fee and insert new ones
                        database.paymentHistoryDao().clearByFee(feeId)
                        database.paymentHistoryDao().insertPaymentHistory(entities)
                        Log.d(TAG, "Saved ${entities.size} payment history records for fee $feeId")
                    }
                    
                    Result.success(payments)
                } else {
                    Result.failure(Exception("Failed to fetch payment history"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing payment history", e)
                // Try to return cached data on error
                val cachedPayments = database.paymentHistoryDao().getPaymentHistoryByFeeSync(feeId)
                if (cachedPayments.isNotEmpty()) {
                    val items = cachedPayments.map { entity ->
                        PaymentHistoryItem(
                            payment_id = entity.payment_id,
                            student_id = entity.student_id,
                            fee_id = entity.fee_id,
                            amount_paid = entity.amount_paid,
                            payment_method = entity.payment_method,
                            remarks = entity.remarks,
                            created_at = entity.created_at
                        )
                    }
                    Result.success(items)
                } else {
                    Result.failure(e)
                }
            }
        }
    }
    
    // ========== Evaluations ==========
    fun getEvaluations(): LiveData<List<EvaluationEntity>> {
        return database.evaluationDao().getAllEvaluations()
    }
    
    suspend fun getEvaluationsOffline(): List<EvaluationEntity> {
        return withContext(Dispatchers.IO) {
            database.evaluationDao().getAllEvaluationsSync()
        }
    }
    
    suspend fun refreshEvaluations(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(context)) {
                    return@withContext Result.success(false)
                }
                
                val response = apiService.getStudentEvaluations()
                if (response.isSuccessful && response.body()?.success == true) {
                    val evaluations = response.body()?.data ?: emptyList()
                    val studentId = sessionManager.getUserId()
                    
                    // Convert to entities and save to Room
                    // Each CourseEvaluation is a single evaluation with course info
                    val entities = evaluations.map { eval ->
                        EvaluationEntity(
                            evaluation_id = eval.evaluation_id,
                            course_id = eval.course_id,
                            teacher_id = eval.teacher_id,
                            evaluation_type_id = eval.evaluation_type_id,
                            evaluation_number = eval.evaluation_number,
                            title = eval.title,
                            description = eval.description,
                            total_marks = eval.total_marks,
                            due_date = eval.due_date,
                            academic_year = eval.academic_year,
                            semester = eval.semester,
                            created_at = eval.created_at,
                            updated_at = eval.updated_at,
                            weightage = null,
                            type_name = eval.type_name,
                            course_name = eval.course_name,
                            course_code = eval.course_code,
                            teacher_name = eval.teacher_name,
                            obtained_marks = eval.obtained_marks,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                    
                    if (entities.isNotEmpty()) {
                        database.evaluationDao().clearAll()
                        database.evaluationDao().insertEvaluations(entities)
                        Log.d(TAG, "Saved ${entities.size} evaluations to database")
                    }
                    
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to fetch evaluations"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing evaluations", e)
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
            database.attendanceSummaryDao().clearAll()
            database.markDao().clearAll()
            database.evaluationDao().clearAll()
            database.notificationDao().clearAll()
            database.classScheduleDao().clearAll()
            database.enrollmentDao().clearAll()
            database.studentFeeDao().clearAll()
            database.paymentHistoryDao().clearAll()
        }
    }
    suspend fun updateFeeAmountsLocally(studentId: Int, feeId: Int, totalAmount: Double, paidAmount: Double) {
        val remainingAmount = totalAmount - paidAmount
        database.studentFeeDao().updateFeeAmounts(studentId, feeId, totalAmount, remainingAmount)
    }

}
