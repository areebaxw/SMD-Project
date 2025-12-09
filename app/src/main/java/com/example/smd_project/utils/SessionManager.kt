package com.example.smd_project.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "CampusinoSession", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PROFILE_PIC = "profile_pic"
        private const val KEY_ROLL_NO = "roll_no"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASSWORD = "saved_password"
    }
    
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    fun saveUserType(userType: String) {
        prefs.edit().putString(KEY_USER_TYPE, userType).apply()
    }
    
    fun getUserType(): String? {
        return prefs.getString(KEY_USER_TYPE, null)
    }
    
    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }
    
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }
    
    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }
    
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }
    
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    fun saveProfilePic(url: String?) {
        prefs.edit().putString(KEY_PROFILE_PIC, url).apply()
    }
    
    fun getProfilePic(): String? {
        return prefs.getString(KEY_PROFILE_PIC, null)
    }
    
    fun saveRollNo(rollNo: String) {
        prefs.edit().putString(KEY_ROLL_NO, rollNo).apply()
    }
    
    fun getRollNo(): String? {
        return prefs.getString(KEY_ROLL_NO, null)
    }
    
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
    
    fun clearSession() {
        // Save biometric settings before clearing
        val biometricEnabled = isBiometricEnabled()
        val savedEmail = getSavedEmail()
        val savedPassword = getSavedPassword()
        
        // Clear all session data
        prefs.edit().clear().apply()
        
        // Restore biometric settings if they existed
        if (biometricEnabled && savedEmail != null && savedPassword != null) {
            setBiometricEnabled(true)
            saveBiometricCredentials(savedEmail, savedPassword)
        }
    }
    
    // Biometric authentication methods
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    fun saveBiometricCredentials(email: String, password: String) {
        prefs.edit().apply {
            putString(KEY_SAVED_EMAIL, email)
            putString(KEY_SAVED_PASSWORD, password)
            apply()
        }
    }
    
    fun getSavedEmail(): String? {
        return prefs.getString(KEY_SAVED_EMAIL, null)
    }
    
    fun getSavedPassword(): String? {
        return prefs.getString(KEY_SAVED_PASSWORD, null)
    }
}
