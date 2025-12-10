package com.example.smd_project.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.smd_project.database.entities.*

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE student_id = :studentId")
    fun getStudent(studentId: Int): LiveData<StudentEntity?>
    
    @Query("SELECT * FROM students WHERE student_id = :studentId")
    suspend fun getStudentSync(studentId: Int): StudentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)
    
    @Update
    suspend fun updateStudent(student: StudentEntity)
    
    @Query("DELETE FROM students")
    suspend fun clearAll()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE is_active = 1")
    fun getAllCourses(): LiveData<List<CourseEntity>>
    
    @Query("SELECT * FROM courses WHERE is_active = 1")
    suspend fun getAllCoursesSync(): List<CourseEntity>
    
    @Query("SELECT * FROM courses WHERE course_id = :courseId")
    suspend fun getCourse(courseId: Int): CourseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)
    
    @Query("DELETE FROM courses")
    suspend fun clearAll()
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE is_active = 1 ORDER BY created_at DESC LIMIT :limit")
    fun getAnnouncements(limit: Int = 50): LiveData<List<AnnouncementEntity>>
    
    @Query("SELECT * FROM announcements WHERE is_active = 1 ORDER BY created_at DESC LIMIT :limit")
    suspend fun getAnnouncementsSync(limit: Int = 50): List<AnnouncementEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)
    
    @Query("DELETE FROM announcements")
    suspend fun clearAll()
    
    @Query("DELETE FROM announcements WHERE last_synced_at < :timestamp")
    suspend fun deleteOldAnnouncements(timestamp: Long)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE student_id = :studentId ORDER BY attendance_date DESC")
    fun getAttendanceByStudent(studentId: Int): LiveData<List<AttendanceEntity>>
    
    @Query("SELECT * FROM attendance WHERE student_id = :studentId ORDER BY attendance_date DESC")
    suspend fun getAttendanceByStudentSync(studentId: Int): List<AttendanceEntity>
    
    @Query("SELECT * FROM attendance WHERE student_id = :studentId AND course_id = :courseId ORDER BY attendance_date DESC")
    fun getAttendanceByStudentAndCourse(studentId: Int, courseId: Int): LiveData<List<AttendanceEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: List<AttendanceEntity>)
    
    @Query("DELETE FROM attendance")
    suspend fun clearAll()
}

@Dao
interface MarkDao {
    @Query("SELECT * FROM marks WHERE student_id = :studentId ORDER BY marked_at DESC")
    fun getMarksByStudent(studentId: Int): LiveData<List<MarkEntity>>
    
    @Query("SELECT * FROM marks WHERE student_id = :studentId ORDER BY marked_at DESC")
    suspend fun getMarksByStudentSync(studentId: Int): List<MarkEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<MarkEntity>)
    
    @Query("DELETE FROM marks")
    suspend fun clearAll()
}

@Dao
interface EvaluationDao {
    @Query("SELECT * FROM evaluations WHERE course_id IN (:courseIds) ORDER BY created_at DESC")
    fun getEvaluationsByCourses(courseIds: List<Int>): LiveData<List<EvaluationEntity>>
    
    @Query("SELECT * FROM evaluations WHERE course_id IN (:courseIds) ORDER BY created_at DESC")
    suspend fun getEvaluationsByCoursesSync(courseIds: List<Int>): List<EvaluationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluations(evaluations: List<EvaluationEntity>)
    
    @Query("DELETE FROM evaluations")
    suspend fun clearAll()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY created_at DESC LIMIT :limit")
    fun getNotifications(limit: Int = 100): LiveData<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications ORDER BY created_at DESC LIMIT :limit")
    suspend fun getNotificationsSync(limit: Int = 100): List<NotificationEntity>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    fun getUnreadCount(): LiveData<Int>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    suspend fun getUnreadCountSync(): Int
    
    @Query("UPDATE notifications SET is_read = 1 WHERE notification_id = :notificationId")
    suspend fun markAsRead(notificationId: Int)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)
    
    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Dao
interface ClassScheduleDao {
    @Query("SELECT * FROM class_schedule WHERE is_active = 1 AND day_of_week = :dayOfWeek ORDER BY start_time ASC")
    fun getScheduleByDay(dayOfWeek: String): LiveData<List<ClassScheduleEntity>>
    
    @Query("SELECT * FROM class_schedule WHERE is_active = 1 ORDER BY CASE day_of_week WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3 WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 WHEN 'Saturday' THEN 6 WHEN 'Sunday' THEN 7 END, start_time ASC")
    fun getAllSchedule(): LiveData<List<ClassScheduleEntity>>
    
    @Query("SELECT * FROM class_schedule WHERE is_active = 1")
    suspend fun getAllScheduleSync(): List<ClassScheduleEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: List<ClassScheduleEntity>)
    
    @Query("DELETE FROM class_schedule")
    suspend fun clearAll()
}

@Dao
interface EnrollmentDao {
    @Query("SELECT * FROM enrollments WHERE student_id = :studentId AND status = 'Enrolled'")
    fun getEnrollmentsByStudent(studentId: Int): LiveData<List<EnrollmentEntity>>
    
    @Query("SELECT * FROM enrollments WHERE student_id = :studentId AND status = 'Enrolled'")
    suspend fun getEnrollmentsByStudentSync(studentId: Int): List<EnrollmentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollments(enrollments: List<EnrollmentEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: EnrollmentEntity)
    
    @Query("DELETE FROM enrollments")
    suspend fun clearAll()
}

@Dao
interface StudentFeeDao {
    @Query("SELECT * FROM student_fees WHERE student_id = :studentId ORDER BY academic_year DESC, semester DESC")
    fun getFeesByStudent(studentId: Int): LiveData<List<StudentFeeEntity>>
    
    @Query("SELECT * FROM student_fees WHERE student_id = :studentId ORDER BY academic_year DESC, semester DESC")
    suspend fun getFeesByStudentSync(studentId: Int): List<StudentFeeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFees(fees: List<StudentFeeEntity>)
    
    @Query("DELETE FROM student_fees")
    suspend fun clearAll()
}
