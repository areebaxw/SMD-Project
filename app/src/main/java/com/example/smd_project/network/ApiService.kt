package com.example.smd_project.network

import com.example.smd_project.models.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<ResponseBody>

    @GET("auth/last-roll-number")
    suspend fun getLastRollNumber(@Query("type") type: String): RollNumberResponse

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): ImageUploadResponse

    @Multipart
    @POST("upload/image/public")
    suspend fun uploadImagePublic(@Part image: MultipartBody.Part): ImageUploadResponse

    @POST("auth/student/signup")
    suspend fun studentSignup(@Body request: SignupRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/student/login")
    suspend fun studentLogin(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/teacher/login")
    suspend fun teacherLogin(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @Multipart
    @POST("auth/upload-profile-picture")
    suspend fun uploadProfilePicture(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<Map<String, String>>>

    // Student Endpoints
    @GET("student/profile")
    suspend fun getStudentProfile(): Response<ApiResponse<Student>>

    @GET("student/dashboard")
    suspend fun getStudentDashboard(@Query("day") dayOfWeek: String? = null): Response<ApiResponse<StudentDashboard>>

    @GET("student/courses")
    suspend fun getStudentCourses(): Response<ApiResponse<List<Course>>>
    @PUT("student/cgpa")
    suspend fun updateCGPA(@Body body: CGPARequest): Response<Any>
    @GET("student/attendance")
    suspend fun getStudentAttendance(): Response<ApiResponse<List<AttendanceSummary>>>

    @GET("student/attendance/{courseId}/details")
    suspend fun getAttendanceDetails(@Path("courseId") courseId: Int): Response<ApiResponse<List<AttendanceRecord>>>

    @GET("student/marks")
    suspend fun getStudentMarks(): Response<ApiResponse<List<Mark>>>

    @GET("student/announcements")
    suspend fun getStudentAnnouncements(): Response<ApiResponse<List<Announcement>>>

    @GET("student/fees")
    suspend fun getStudentFees(): Response<ApiResponse<List<StudentFeeItem>>>

    @GET("student/notifications")
    suspend fun getStudentNotifications(@Query("limit") limit: Int = 50): Response<ApiResponse<List<Notification>>>

    @GET("student/notifications/unread-count")
    suspend fun getUnreadNotificationsCount(): Response<ApiResponse<UnreadCountResponse>>

    @POST("student/notifications/{notificationId}/mark-read")
    suspend fun markStudentNotificationAsRead(@Path("notificationId") notificationId: Int): Response<ApiResponse<Any>>

    @POST("student/fcm-token")
    suspend fun registerFCMToken(@Body body: Map<String, String>): Response<ApiResponse<Any>>


    @GET("student/transcript")
    suspend fun getStudentTranscript(): Response<ApiResponse<Map<String, Any>>>

    // Teacher Endpoints
    @GET("teacher/profile")
    suspend fun getTeacherProfile(): Response<ApiResponse<Teacher>>

    @GET("teacher/dashboard")
    suspend fun getTeacherDashboard(): Response<ApiResponse<TeacherDashboard>>

    @GET("teacher/courses")
    suspend fun getTeacherCourses(): Response<ApiResponse<List<Course>>>

    @GET("teacher/course/{courseId}/students")
    suspend fun getCourseStudents(@Path("courseId") courseId: Int): Response<ApiResponse<List<Student>>>

    @POST("teacher/mark-attendance")
    suspend fun markAttendance(@Body request: MarkAttendanceRequest): Response<ApiResponse<MarkAttendanceResponse>>

    @POST("teacher/enter-marks")
    suspend fun enterMarks(@Body request: EnterMarksRequest): Response<ApiResponse<Any>>

    @POST("teacher/post-announcement")
    suspend fun postAnnouncement(@Body request: PostAnnouncementRequest): Response<ApiResponse<PostAnnouncementResponse>>


    @GET("teacher/announcements")
    suspend fun getTeacherAnnouncements(): Response<ApiResponse<List<Announcement>>>

    @GET("teacher/schedule")
    suspend fun getTeacherSchedule(): Response<ApiResponse<List<Schedule>>>

    @GET("teacher/schedule/today")
    suspend fun getTodaySchedule(): Response<ApiResponse<List<Schedule>>>

    @GET("teacher/announcements/all")
    suspend fun getTeacherAllAnnouncements(): Response<ApiResponse<List<Announcement>>>

    @GET("teacher/notifications")
    suspend fun getTeacherNotifications(@Query("limit") limit: Int = 50): Response<ApiResponse<List<Notification>>>

    @GET("teacher/notifications/unread")
    suspend fun getUnreadNotifications(): Response<ApiResponse<UnreadCountResponse>>

    @POST("teacher/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: Int): Response<ApiResponse<Any>>

    @GET("teacher/course/{courseId}/evaluations")
    suspend fun getCourseEvaluations(@Path("courseId") courseId: Int): Response<ApiResponse<List<Evaluation>>>

    @GET("teacher/evaluation-types")
    suspend fun getEvaluationTypes(): Response<ApiResponse<List<EvaluationType>>>

    @POST("teacher/create-evaluation")
    suspend fun createEvaluation(@Body request: CreateEvaluationRequest): Response<ApiResponse<CreateEvaluationResponse>>

    @POST("teacher/mark-student-assessment")
    suspend fun markStudentAssessment(@Body request: MarkStudentAssessmentRequest): Response<ApiResponse<MarkStudentAssessmentResponse>>

    @GET("teacher/course/{courseId}/attendance-summary")
    suspend fun getCourseAttendanceSummary(@Path("courseId") courseId: Int): Response<ApiResponse<List<AttendanceSummary>>>

    @GET("teacher/course/{courseId}/attendance-today")
    suspend fun getTodayAttendance(
        @Path("courseId") courseId: Int,
        @Query("date") date: String? = null  // Optional: defaults to today if not provided
    ): Response<ApiResponse<List<TodayAttendanceItem>>>

    @GET("teacher/course/{courseId}/marks")
    suspend fun getCourseMarks(@Path("courseId") courseId: Int): Response<ApiResponse<List<CourseMarks>>>

    @GET("teacher/course/{courseId}/evaluation-type/{evaluationTypeId}/marks")
    suspend fun getEvaluationMarks(
        @Path("courseId") courseId: Int,
        @Path("evaluationTypeId") evaluationTypeId: Int
    ): Response<ApiResponse<EvaluationMarksResponse>>

    @DELETE("teacher/evaluation/{evaluationId}")
    suspend fun deleteEvaluation(@Path("evaluationId") evaluationId: Int): Response<ApiResponse<Any>>

    // Course Registration Endpoints
    @GET("student/courses/available")
    suspend fun getAvailableCourses(): Response<ApiResponse<List<Course>>>

    @GET("student/courses/registered")
    suspend fun getRegisteredCourses(): Response<ApiResponse<List<Course>>>

    @POST("student/courses/register")
    suspend fun registerCourses(@Body request: CourseRegistrationRequest): Response<ApiResponse<CourseRegistrationResponse>>

    @POST("student/enroll")
    suspend fun enrollInCourses(@Body request: EnrollCoursesRequest): Response<ApiResponse<Map<String, Any>>>

    @POST("student/enroll/single")
    suspend fun enrollInCourse(@Body request: EnrollCourseRequest): Response<ApiResponse<Map<String, Any>>>

    @DELETE("student/drop/{courseId}")
    suspend fun dropCourse(@Path("courseId") courseId: Int): Response<ApiResponse<Any>>

    // New Student Endpoints for Dashboard
    @GET("student/courses")
    suspend fun getStudentEnrolledCourses(): Response<ApiResponse<List<StudentCourse>>>

    @GET("student/marks")
    suspend fun getStudentAllMarks(): Response<ApiResponse<List<CourseMarksDetail>>>

    @GET("student/evaluations")
    suspend fun getStudentEvaluations(): Response<ApiResponse<List<CourseEvaluation>>>

    @GET("student/fees")
    suspend fun getStudentFeesInfo(): Response<ApiResponse<List<StudentFee>>>

    @GET("student/attendance")
    suspend fun getStudentAttendanceSummary(): Response<ApiResponse<List<AttendanceSummary>>>

    @GET("student/schedule")
    suspend fun getStudentSchedule(): Response<ApiResponse<List<Schedule>>>

    @POST("/api/student/fees/update-total")
    suspend fun updateStudentFeeTotal(@Body request: UpdateTotalFeeRequest): Response<ApiResponse<Any>>

    @POST("student/fees/pay")
    suspend fun payStudentFee(@Body request: PayFeeRequest): Response<ApiResponse<Any>>

    // Get payment history for a specific fee
    @GET("student/fees/history/{feeId}")
    suspend fun getFeePaymentHistory(@Path("feeId") feeId: Int): Response<ApiResponse<List<PaymentHistoryItem>>>

    @POST("teacher/upload-final-grades")
    suspend fun uploadFinalGrades(@Body request: UploadFinalGradesRequest): Response<ApiResponse<Any>>

    //get course details
    @GET("student/course-details/{course_id}")
    suspend fun getCourseDetails(@Path("course_id") courseId: Int): Response<CourseResponse>

}
