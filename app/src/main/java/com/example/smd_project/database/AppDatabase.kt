package com.example.smd_project.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smd_project.database.dao.*
import com.example.smd_project.database.entities.*

@Database(
    entities = [
        StudentEntity::class,
        CourseEntity::class,
        AnnouncementEntity::class,
        AttendanceEntity::class,
        MarkEntity::class,
        EvaluationEntity::class,
        NotificationEntity::class,
        ClassScheduleEntity::class,
        EnrollmentEntity::class,
        StudentFeeEntity::class,
        // Teacher entities
        TeacherEntity::class,
        TeacherCourseEntity::class,
        TeacherScheduleEntity::class,
        TeacherAnnouncementEntity::class,
        TeacherAttendanceRecordEntity::class,
        TeacherMarkRecordEntity::class,
        PendingSyncEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun studentDao(): StudentDao
    abstract fun courseDao(): CourseDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun markDao(): MarkDao
    abstract fun evaluationDao(): EvaluationDao
    abstract fun notificationDao(): NotificationDao
    abstract fun classScheduleDao(): ClassScheduleDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun studentFeeDao(): StudentFeeDao
    
    // Teacher DAOs
    abstract fun teacherDao(): TeacherDao
    abstract fun teacherCourseDao(): TeacherCourseDao
    abstract fun teacherScheduleDao(): TeacherScheduleDao
    abstract fun teacherAnnouncementDao(): TeacherAnnouncementDao
    abstract fun teacherAttendanceRecordDao(): TeacherAttendanceRecordDao
    abstract fun teacherMarkRecordDao(): TeacherMarkRecordDao
    abstract fun pendingSyncDao(): PendingSyncDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smd_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
