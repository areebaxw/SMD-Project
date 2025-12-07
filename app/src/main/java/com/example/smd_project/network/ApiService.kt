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
    suspend fun markAttendance(@Body request: MarkAttendanceRequest): Response<ApiResponse<Any>>
    
    @POST("teacher/enter-marks")
    suspend fun enterMarks(@Body request: EnterMarksRequest): Response<ApiResponse<Any>>
    
    @POST("teacher/post-announcement")
    suspend fun postAnnouncement(@Body request: PostAnnouncementRequest): Response<ApiResponse<Map<String, Int>>>
    
    @GET("teacher/announcements")
    suspend fun getTeacherAnnouncements(): Response<ApiResponse<List<Announcement>>>
    
    @GET("teacher/schedule")
    suspend fun getTeacherSchedule(): Response<ApiResponse<List<TodayClass>>>
}
