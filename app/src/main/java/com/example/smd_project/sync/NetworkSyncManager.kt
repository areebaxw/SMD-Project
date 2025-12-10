package com.example.smd_project.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import com.example.smd_project.utils.NetworkUtils

/**
 * Monitors network connectivity changes and triggers sync when device comes back online
 */
class NetworkSyncManager(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkSyncManager"
        
        @Volatile
        private var INSTANCE: NetworkSyncManager? = null
        
        fun getInstance(context: Context): NetworkSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkSyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isRegistered = false
    
    /**
     * Start monitoring network changes
     */
    fun startMonitoring() {
        if (isRegistered) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(TAG, "Network available - triggering sync")
                    onNetworkAvailable()
                }
                
                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d(TAG, "Network lost")
                }
                
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    
                    if (hasInternet && isValidated) {
                        Log.d(TAG, "Network validated - triggering sync")
                        onNetworkAvailable()
                    }
                }
            }
            
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
            isRegistered = true
            Log.d(TAG, "Network monitoring started")
        }
    }
    
    /**
     * Stop monitoring network changes
     */
    fun stopMonitoring() {
        if (!isRegistered) return
        
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering network callback", e)
            }
        }
        isRegistered = false
        Log.d(TAG, "Network monitoring stopped")
    }
    
    /**
     * Called when network becomes available
     */
    private fun onNetworkAvailable() {
        // Trigger both student and teacher sync
        try {
            // For student data
            com.example.smd_project.utils.SyncManager.syncNow(context)
            
            // For teacher data
            TeacherSyncWorker.syncNow(context)
            
            Log.d(TAG, "Sync triggered after network available")
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering sync", e)
        }
    }
}

/**
 * BroadcastReceiver for network connectivity changes (legacy support)
 */
class NetworkChangeReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "NetworkChangeReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            if (NetworkUtils.isOnline(context)) {
                Log.d(TAG, "Network connected - triggering sync")
                
                // Trigger sync
                com.example.smd_project.utils.SyncManager.syncNow(context)
                TeacherSyncWorker.syncNow(context)
            }
        }
    }
}
