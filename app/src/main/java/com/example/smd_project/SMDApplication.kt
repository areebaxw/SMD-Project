package com.example.smd_project

import android.app.Application
import com.example.smd_project.sync.NetworkSyncManager

class SMDApplication : Application() {
    
    private lateinit var networkSyncManager: NetworkSyncManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize network sync manager to auto-sync when online
        networkSyncManager = NetworkSyncManager.getInstance(this)
        networkSyncManager.startMonitoring()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        networkSyncManager.stopMonitoring()
    }
}
