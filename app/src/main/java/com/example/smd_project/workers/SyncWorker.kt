package com.example.smd_project.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smd_project.repository.StudentRepository
import com.example.smd_project.utils.NetworkUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val repository = StudentRepository(context)
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "student_data_sync"
        const val SYNC_TYPE_KEY = "sync_type"
        const val SYNC_TYPE_FULL = "full"
        const val SYNC_TYPE_DASHBOARD = "dashboard"
        const val SYNC_TYPE_NOTIFICATIONS = "notifications"
    }
    
    override suspend fun doWork(): Result {
        return try {
            // Check network connectivity
            if (!NetworkUtils.isOnline(applicationContext)) {
                Log.d(TAG, "No network connection, skipping sync")
                return Result.retry()
            }
            
            val syncType = inputData.getString(SYNC_TYPE_KEY) ?: SYNC_TYPE_FULL
            Log.d(TAG, "Starting sync: $syncType")
            
            when (syncType) {
                SYNC_TYPE_DASHBOARD -> syncDashboard()
                SYNC_TYPE_NOTIFICATIONS -> syncNotifications()
                SYNC_TYPE_FULL -> syncAll()
                else -> syncAll()
            }
            
            Log.d(TAG, "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            // Retry on failure
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun syncDashboard() {
        try {
            repository.getDashboardData(forceRefresh = true)
            Log.d(TAG, "Dashboard synced")
        } catch (e: Exception) {
            Log.e(TAG, "Dashboard sync failed", e)
            throw e
        }
    }
    
    private suspend fun syncNotifications() {
        try {
            repository.refreshNotifications()
            Log.d(TAG, "Notifications synced")
        } catch (e: Exception) {
            Log.e(TAG, "Notifications sync failed", e)
            throw e
        }
    }
    
    private suspend fun syncAll() = coroutineScope {
        try {
            // Sync all data in parallel
            val results = listOf(
                async { repository.getDashboardData(forceRefresh = true) },
                async { repository.refreshCourses() },
                async { repository.refreshMarks() },
                async { repository.refreshAttendance() },
                async { repository.refreshNotifications() },
                async { repository.refreshFees() }
            ).awaitAll()
            
            // Check if any failed
            val failed = results.count { it.isFailure }
            if (failed > 0) {
                Log.w(TAG, "$failed sync operations failed")
            } else {
                Log.d(TAG, "All data synced successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed", e)
            throw e
        }
    }
}
