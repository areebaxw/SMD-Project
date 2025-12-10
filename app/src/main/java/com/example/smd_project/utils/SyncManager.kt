package com.example.smd_project.utils

import android.content.Context
import androidx.work.*
import com.example.smd_project.workers.SyncWorker
import java.util.concurrent.TimeUnit

object SyncManager {
    
    private const val TAG = "SyncManager"
    
    /**
     * Schedule periodic sync - runs every 15 minutes when app is running
     */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    /**
     * Trigger immediate one-time sync
     */
    fun syncNow(context: Context, syncType: String = SyncWorker.SYNC_TYPE_FULL) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val inputData = workDataOf(SyncWorker.SYNC_TYPE_KEY to syncType)
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueue(syncRequest)
    }
    
    /**
     * Sync dashboard data only
     */
    fun syncDashboard(context: Context) {
        syncNow(context, SyncWorker.SYNC_TYPE_DASHBOARD)
    }
    
    /**
     * Sync notifications only
     */
    fun syncNotifications(context: Context) {
        syncNow(context, SyncWorker.SYNC_TYPE_NOTIFICATIONS)
    }
    
    /**
     * Cancel all sync work
     */
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.WORK_NAME)
    }
    
    /**
     * Get sync status
     */
    fun getSyncStatus(context: Context) = 
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
}
