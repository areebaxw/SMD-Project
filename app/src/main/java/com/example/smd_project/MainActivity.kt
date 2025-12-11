package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.models.LoginRequest
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnBiometricLogin: ImageButton
    private lateinit var rgUserType: RadioGroup
    private lateinit var rbStudent: RadioButton
    private lateinit var rbTeacher: RadioButton
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard()
            return
        }
        
        setContentView(R.layout.activity_login)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        etEmail = findViewById(R.id.input_email)
        etPassword = findViewById(R.id.input_password)
        btnLogin = findViewById(R.id.btn_login)
        btnBiometricLogin = findViewById(R.id.btn_biometric_login)
        
        // Setup signup click listener
        findViewById<TextView>(R.id.txt_signup)?.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        
        // Setup biometric authentication
        setupBiometricAuth()
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            login(email, password, enableBiometric = true)
        }
        
        btnBiometricLogin.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }
    
    private fun login(email: String, password: String, enableBiometric: Boolean = false) {
        val loginRequest = LoginRequest(email, password)
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                // Try student login first (you can add user type selection)
                val response = apiService.studentLogin(loginRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()?.data
                    
                    if (loginResponse != null) {
                        // Save session data
                        sessionManager.saveAuthToken(loginResponse.token)
                        Log.d("DEBUG_TOKEN", "Token saved: ${sessionManager.getToken()}")

                        sessionManager.saveUserType("Student")
                        
                        loginResponse.student?.let { student ->
                            sessionManager.saveUserId(student.student_id)
                            sessionManager.saveUserEmail(student.email)
                            sessionManager.saveUserName(student.full_name)
                            sessionManager.saveRollNo(student.roll_no)
                            sessionManager.saveProfilePic(student.profile_picture_url)

                        }
                        
                        // Save credentials for biometric login if requested
                        if (enableBiometric) {
                            sessionManager.saveBiometricCredentials(email, password)
                            sessionManager.setBiometricEnabled(true)
                        }
                        
                        Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        navigateToDashboard()
                    }
                } else {
                    // Try teacher login
                    val teacherResponse = apiService.teacherLogin(loginRequest)
                    
                    if (teacherResponse.isSuccessful && teacherResponse.body()?.success == true) {
                        val loginResponse = teacherResponse.body()?.data
                        
                        if (loginResponse != null) {
                            sessionManager.saveAuthToken(loginResponse.token)
                            Log.d("DEBUG_TOKEN", "Token saved: ${sessionManager.getToken()}")

                            sessionManager.saveUserType("Teacher")
                            
                            loginResponse.teacher?.let { teacher ->
                                sessionManager.saveUserId(teacher.teacher_id)
                                sessionManager.saveUserEmail(teacher.email)
                                sessionManager.saveUserName(teacher.full_name)
                                sessionManager.saveProfilePic(teacher.profile_picture_url)
                                sessionManager.saveEmployeeId(teacher.employee_id)

                            }
                            
                            // Save credentials for biometric login if requested
                            if (enableBiometric) {
                                sessionManager.saveBiometricCredentials(email, password)
                                sessionManager.setBiometricEnabled(true)
                            }
                            
                            Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun navigateToDashboard() {
        val userType = sessionManager.getUserType()
        val intent = if (userType == "Student") {
            Intent(this, StudentDashboard::class.java)
        } else {
            Intent(this, TeacherDashboard::class.java)
        }
        startActivity(intent)
        finish()
    }
    
    private fun setupBiometricAuth() {
        executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e("BiometricAuth", "Authentication error: $errorCode - $errString")
                    Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("BiometricAuth", "Authentication succeeded!")
                    val cryptoObject = result.cryptoObject
                    Log.d("BiometricAuth", "Crypto object: ${cryptoObject != null}")
                    Toast.makeText(applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show()

                    // Use saved credentials to login
                    val savedEmail = sessionManager.getSavedEmail()
                    val savedPassword = sessionManager.getSavedPassword()

                    Log.d("BiometricAuth", "Saved email: ${savedEmail?.take(3)}..., password exists: ${savedPassword != null}")

                    if (savedEmail != null && savedPassword != null) {
                        login(savedEmail, savedPassword, enableBiometric = false)
                    } else {
                        Log.e("BiometricAuth", "No saved credentials found")
                        Toast.makeText(applicationContext,
                            "No saved credentials found. Please login normally first.",
                            Toast.LENGTH_LONG).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w("BiometricAuth", "Authentication failed - biometric not recognized")
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your fingerprint, face, or device PIN/pattern")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                    BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .setNegativeButtonText("Use Password")
            .build()
        
        // Check if biometric is available on device
        checkBiometricAvailability()
    }
    
    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)

        // Check different authenticator combinations
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val strongResult = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        val weakResult = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        val deviceResult = biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        val combinedResult = biometricManager.canAuthenticate(authenticators)

        Log.d("BiometricAuth", "Strong: $strongResult, Weak: $weakResult, Device: $deviceResult, Combined: $combinedResult")

        when (combinedResult) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("BiometricAuth", "App can authenticate using biometrics.")
                // Only show biometric button if user has enabled it before
                if (sessionManager.isBiometricEnabled()) {
                    btnBiometricLogin.visibility = android.view.View.VISIBLE
                    Log.d("BiometricAuth", "Biometric login button shown")
                } else {
                    Log.d("BiometricAuth", "Biometric enabled but not set in session")
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("BiometricAuth", "No biometric features available on this device.")
                // Try with just device credential
                if (deviceResult == BiometricManager.BIOMETRIC_SUCCESS) {
                    Log.d("BiometricAuth", "Device credential authentication available.")
                    if (sessionManager.isBiometricEnabled()) {
                        btnBiometricLogin.visibility = android.view.View.VISIBLE
                        Log.d("BiometricAuth", "Biometric login button shown (device credential)")
                    }
                }
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("BiometricAuth", "Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e("BiometricAuth", "The user hasn't associated any biometric credentials with their account.")
                // Check if device credential is available
                if (deviceResult == BiometricManager.BIOMETRIC_SUCCESS) {
                    Log.d("BiometricAuth", "Device credential available as fallback.")
                    if (sessionManager.isBiometricEnabled()) {
                        btnBiometricLogin.visibility = android.view.View.VISIBLE
                        Log.d("BiometricAuth", "Biometric login button shown (device credential fallback)")
                    }
                }
            }
        }
    }
}