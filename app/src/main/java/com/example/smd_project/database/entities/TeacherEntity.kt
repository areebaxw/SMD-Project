package com.example.smd_project.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey
    val teacher_id: Int,
    val employee_id: String,
    val full_name: String,
    val email: String,
    val profile_picture_url: String?,
    val phone: String?,
    val department: String?,
    val designation: String?,
    val specialization: String?,
    val is_active: Int,
    val created_at: String?,
    val last_synced_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "teacher_courses")
data class TeacherCourseEntity(
    @PrimaryKey
    val course_id: Int,
    val teacher_id: Int,
    val course_code: String,
    val course_name: String,
    val credit_hours: Int,
    val semester: Int?,
    val section: String?,
    val enrolled_students: Int = 0,
    val last_synced_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "teacher_schedule")
data class TeacherScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val teacher_id: Int,
    val course_id: Int,
    val course_code: String,
    val course_name: String,
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val room: String?,
    val last_synced_at: Long = System.currentTimeMillis()
)

@Entity(tableName = "teacher_announcements")
data class TeacherAnnouncementEntity(
    @PrimaryKey
    val announcement_id: Int,
    val teacher_id: Int,
    val course_id: Int?,
    val title: String,
    val content: String,
    val created_at: String,
    val is_synced: Boolean = true,
    val last_synced_at: Long = System.currentTimeMillis()
)

// For offline queue - pending operations to sync
@Entity(tableName = "pending_sync_queue")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val operation_type: String, // "MARK_ATTENDANCE", "UPDATE_MARKS", "POST_ANNOUNCEMENT"
    val entity_type: String, // "attendance", "marks", "announcement"
    val entity_id: Int?,
    val payload: String, // JSON payload of the data
    val created_at: Long = System.currentTimeMillis(),
    val retry_count: Int = 0,
    val last_retry_at: Long? = null
)

// For storing attendance records marked by teacher
@Entity(tableName = "teacher_attendance_records", primaryKeys = ["course_id", "student_id", "attendance_date"])
data class TeacherAttendanceRecordEntity(
    val course_id: Int,
    val student_id: Int,
    val student_name: String,
    val student_roll_no: String,
    val attendance_date: String,
    val status: String, // "present", "absent", "late"
    val is_synced: Boolean = true,
    val last_synced_at: Long = System.currentTimeMillis()
)

// For storing marks entered by teacher
@Entity(tableName = "teacher_marks_records", primaryKeys = ["course_id", "student_id", "evaluation_type_id"])
data class TeacherMarkRecordEntity(
    val course_id: Int,
    val student_id: Int,
    val student_name: String,
    val student_roll_no: String,
    val evaluation_type_id: Int,
    val evaluation_type: String,
    val marks_obtained: Double,
    val total_marks: Double,
    val is_synced: Boolean = true,
    val last_synced_at: Long = System.currentTimeMillis()
)
