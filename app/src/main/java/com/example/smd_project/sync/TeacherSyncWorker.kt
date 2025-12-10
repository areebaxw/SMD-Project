package com.example.smd_project.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.smd_project.repository.TeacherRepository
import com.example.smd_project.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class TeacherSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "TeacherSyncWorker"
        private const val WORK_NAME = "teacher_sync_work"
        
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<TeacherSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
            
            Log.d(TAG, "Scheduled periodic teacher sync")
        }
        
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = OneTimeWorkRequestBuilder<TeacherSyncWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context)
                .enqueue(syncRequest)
            
            Log.d(TAG, "Triggered immediate teacher sync")
        }
        
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled teacher sync")
        }
    }
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isOnline(applicationContext)) {
                    Log.d(TAG, "No network, skipping sync")
                    return@withContext Result.retry()
                }
                
                Log.d(TAG, "Starting teacher sync...")
                
                val repository = TeacherRepository(applicationContext)
                
                // Sync pending changes (attendance, marks, announcements)
                val syncResult = repository.syncPendingChanges()
                val syncedCount = syncResult.getOrDefault(0)
                Log.d(TAG, "Synced $syncedCount pending items")
                
                // Refresh dashboard data
                repository.getDashboardData(forceRefresh = true)
                Log.d(TAG, "Refreshed teacher dashboard cache")
                
                // Refresh courses
                repository.getCourses(forceRefresh = true)
                Log.d(TAG, "Refreshed teacher courses cache")
                
                // Refresh announcements
                repository.getAnnouncements(forceRefresh = true)
                Log.d(TAG, "Refreshed teacher announcements cache")
                
                Log.d(TAG, "Teacher sync completed successfully")
                Result.success()
                
            } catch (e: Exception) {
                Log.e(TAG, "Teacher sync failed", e)
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }
}
