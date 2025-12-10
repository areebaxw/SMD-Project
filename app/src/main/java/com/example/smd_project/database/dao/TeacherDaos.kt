package com.example.smd_project.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.smd_project.database.entities.*

@Dao
interface TeacherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)
    
    @Query("SELECT * FROM teachers WHERE teacher_id = :teacherId")
    suspend fun getTeacher(teacherId: Int): TeacherEntity?
    
    @Query("SELECT * FROM teachers WHERE teacher_id = :teacherId")
    fun getTeacherLive(teacherId: Int): LiveData<TeacherEntity?>
    
    @Query("DELETE FROM teachers")
    suspend fun deleteAll()
}

@Dao
interface TeacherCourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<TeacherCourseEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: TeacherCourseEntity)
    
    @Query("SELECT * FROM teacher_courses WHERE teacher_id = :teacherId")
    fun getCourses(teacherId: Int): LiveData<List<TeacherCourseEntity>>
    
    @Query("SELECT * FROM teacher_courses WHERE teacher_id = :teacherId")
    suspend fun getCoursesSync(teacherId: Int): List<TeacherCourseEntity>
    
    @Query("SELECT * FROM teacher_courses WHERE course_id = :courseId")
    suspend fun getCourse(courseId: Int): TeacherCourseEntity?
    
    @Query("DELETE FROM teacher_courses WHERE teacher_id = :teacherId")
    suspend fun deleteByTeacher(teacherId: Int)
    
    @Query("DELETE FROM teacher_courses")
    suspend fun deleteAll()
}

@Dao
interface TeacherScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<TeacherScheduleEntity>)
    
    @Query("SELECT * FROM teacher_schedule WHERE teacher_id = :teacherId ORDER BY CASE day_of_week WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3 WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 WHEN 'Saturday' THEN 6 WHEN 'Sunday' THEN 7 END, start_time")
    fun getSchedules(teacherId: Int): LiveData<List<TeacherScheduleEntity>>
    
    @Query("SELECT * FROM teacher_schedule WHERE teacher_id = :teacherId AND day_of_week = :day ORDER BY start_time")
    fun getTodaySchedule(teacherId: Int, day: String): LiveData<List<TeacherScheduleEntity>>
    
    @Query("SELECT * FROM teacher_schedule WHERE teacher_id = :teacherId AND day_of_week = :day ORDER BY start_time")
    suspend fun getTodayScheduleSync(teacherId: Int, day: String): List<TeacherScheduleEntity>
    
    @Query("DELETE FROM teacher_schedule WHERE teacher_id = :teacherId")
    suspend fun deleteByTeacher(teacherId: Int)
    
    @Query("DELETE FROM teacher_schedule")
    suspend fun deleteAll()
}

@Dao
interface TeacherAnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<TeacherAnnouncementEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: TeacherAnnouncementEntity)
    
    @Query("SELECT * FROM teacher_announcements WHERE teacher_id = :teacherId ORDER BY created_at DESC")
    fun getAnnouncements(teacherId: Int): LiveData<List<TeacherAnnouncementEntity>>
    
    @Query("SELECT * FROM teacher_announcements WHERE teacher_id = :teacherId ORDER BY created_at DESC")
    suspend fun getAnnouncementsSync(teacherId: Int): List<TeacherAnnouncementEntity>
    
    @Query("SELECT * FROM teacher_announcements WHERE is_synced = 0")
    suspend fun getUnsyncedAnnouncements(): List<TeacherAnnouncementEntity>
    
    @Query("UPDATE teacher_announcements SET is_synced = 1 WHERE announcement_id = :announcementId")
    suspend fun markSynced(announcementId: Int)
    
    @Query("DELETE FROM teacher_announcements WHERE teacher_id = :teacherId")
    suspend fun deleteByTeacher(teacherId: Int)
    
    @Query("DELETE FROM teacher_announcements")
    suspend fun deleteAll()
}

@Dao
interface PendingSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pendingSync: PendingSyncEntity): Long
    
    @Query("SELECT * FROM pending_sync_queue ORDER BY created_at ASC")
    suspend fun getAllPending(): List<PendingSyncEntity>
    
    @Query("SELECT * FROM pending_sync_queue WHERE operation_type = :operationType ORDER BY created_at ASC")
    suspend fun getPendingByType(operationType: String): List<PendingSyncEntity>
    
    @Query("SELECT COUNT(*) FROM pending_sync_queue")
    fun getPendingCount(): LiveData<Int>
    
    @Query("SELECT COUNT(*) FROM pending_sync_queue")
    suspend fun getPendingCountSync(): Int
    
    @Delete
    suspend fun delete(pendingSync: PendingSyncEntity)
    
    @Query("DELETE FROM pending_sync_queue WHERE id = :id")
    suspend fun deleteById(id: Int)
    
    @Query("UPDATE pending_sync_queue SET retry_count = retry_count + 1, last_retry_at = :timestamp WHERE id = :id")
    suspend fun incrementRetry(id: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM pending_sync_queue")
    suspend fun deleteAll()
}

@Dao
interface TeacherAttendanceRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<TeacherAttendanceRecordEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TeacherAttendanceRecordEntity)
    
    @Query("SELECT * FROM teacher_attendance_records WHERE course_id = :courseId AND attendance_date = :date")
    fun getAttendanceForDate(courseId: Int, date: String): LiveData<List<TeacherAttendanceRecordEntity>>
    
    @Query("SELECT * FROM teacher_attendance_records WHERE course_id = :courseId AND attendance_date = :date")
    suspend fun getAttendanceForDateSync(courseId: Int, date: String): List<TeacherAttendanceRecordEntity>
    
    @Query("SELECT * FROM teacher_attendance_records WHERE is_synced = 0")
    suspend fun getUnsyncedRecords(): List<TeacherAttendanceRecordEntity>
    
    @Query("UPDATE teacher_attendance_records SET is_synced = 1 WHERE course_id = :courseId AND student_id = :studentId AND attendance_date = :date")
    suspend fun markSynced(courseId: Int, studentId: Int, date: String)
    
    @Query("DELETE FROM teacher_attendance_records WHERE course_id = :courseId")
    suspend fun deleteByCourse(courseId: Int)
    
    @Query("DELETE FROM teacher_attendance_records")
    suspend fun deleteAll()
}

@Dao
interface TeacherMarkRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<TeacherMarkRecordEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TeacherMarkRecordEntity)
    
    @Query("SELECT * FROM teacher_marks_records WHERE course_id = :courseId AND evaluation_type_id = :evalTypeId")
    fun getMarksForEvaluation(courseId: Int, evalTypeId: Int): LiveData<List<TeacherMarkRecordEntity>>
    
    @Query("SELECT * FROM teacher_marks_records WHERE course_id = :courseId AND evaluation_type_id = :evalTypeId")
    suspend fun getMarksForEvaluationSync(courseId: Int, evalTypeId: Int): List<TeacherMarkRecordEntity>
    
    @Query("SELECT * FROM teacher_marks_records WHERE is_synced = 0")
    suspend fun getUnsyncedRecords(): List<TeacherMarkRecordEntity>
    
    @Query("UPDATE teacher_marks_records SET is_synced = 1 WHERE course_id = :courseId AND student_id = :studentId AND evaluation_type_id = :evalTypeId")
    suspend fun markSynced(courseId: Int, studentId: Int, evalTypeId: Int)
    
    @Query("DELETE FROM teacher_marks_records WHERE course_id = :courseId")
    suspend fun deleteByCourse(courseId: Int)
    
    @Query("DELETE FROM teacher_marks_records")
    suspend fun deleteAll()
}
