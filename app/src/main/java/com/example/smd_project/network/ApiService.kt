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
    suspend fun getStudentDashboard(): Response<ApiResponse<StudentDashboard>>

    @GET("student/courses")
    suspend fun getStudentCourses(): Response<ApiResponse<List<Course>>>

    @GET("student/attendance")
    suspend fun getStudentAttendance(): Response<ApiResponse<List<AttendanceSummary>>>

    @GET("student/attendance/{courseId}/details")
    suspend fun getAttendanceDetails(@Path("courseId") courseId: Int): Response<ApiResponse<List<AttendanceRecord>>>

    @GET("student/marks")
    suspend fun getStudentMarks(): Response<ApiResponse<MarksResponse>>

    @GET("student/announcements")
    suspend fun getStudentAnnouncements(): Response<ApiResponse<List<Announcement>>>

    @GET("student/fees")
    suspend fun getStudentFees(): Response<ApiResponse<FeeDetails>>

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
    suspend fun getTodayAttendance(@Path("courseId") courseId: Int): Response<ApiResponse<List<TodayAttendanceItem>>>

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

    @DELETE("student/courses/drop/{courseId}")
    suspend fun dropCourse(@Path("courseId") courseId: Int): Response<ApiResponse<Any>>
}
