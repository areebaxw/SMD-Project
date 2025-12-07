package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.models.LoginRequest
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var rgUserType: RadioGroup
    private lateinit var rbStudent: RadioButton
    private lateinit var rbTeacher: RadioButton
    
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
        
        // Setup signup click listener
        findViewById<TextView>(R.id.txt_signup)?.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            login(email, password)
        }
    }
    
    private fun login(email: String, password: String) {
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
                        sessionManager.saveUserType("Student")
                        
                        loginResponse.student?.let { student ->
                            sessionManager.saveUserId(student.student_id)
                            sessionManager.saveUserEmail(student.email)
                            sessionManager.saveUserName(student.full_name)
                            sessionManager.saveProfilePic(student.profile_picture_url)
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
                            sessionManager.saveUserType("Teacher")
                            
                            loginResponse.teacher?.let { teacher ->
                                sessionManager.saveUserId(teacher.teacher_id)
                                sessionManager.saveUserEmail(teacher.email)
                                sessionManager.saveUserName(teacher.full_name)
                                sessionManager.saveProfilePic(teacher.profile_picture_url)
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
}