package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.smd_project.utils.SessionManager

class SplashActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private val SPLASH_DELAY = 3000L // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        sessionManager = SessionManager(this)
        
        // Clear session on app restart (force logout)
        sessionManager.clearSession()
        
        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DELAY)
    }
    
    private fun navigateToNextScreen() {
        // Always go to login screen after splash
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close splash screen
    }
}
