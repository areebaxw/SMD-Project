package com.example.smd_project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.models.SignupRequest
import com.example.smd_project.models.SignupResponse
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class SignupActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var txtLogin: TextView
    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnSelectImage: ImageView
    private lateinit var btnStudent: TextView
    private lateinit var btnTeacher: TextView
    
    private var selectedImageUri: Uri? = null
    private var selectedRole: String = "Student" // Default to Student
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                ivProfilePicture.setImageURI(uri)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied to read images", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        etFullName = findViewById(R.id.input_fullname)
        etEmail = findViewById(R.id.input_email)
        etPassword = findViewById(R.id.input_password)
        etConfirmPassword = findViewById(R.id.input_confirm_password)
        btnSignup = findViewById(R.id.btn_signup)
        txtLogin = findViewById(R.id.txt_login)
        ivProfilePicture = findViewById(R.id.iv_profile_picture)
        btnSelectImage = findViewById(R.id.btn_select_image)
        btnStudent = findViewById(R.id.btn_student)
        btnTeacher = findViewById(R.id.btn_teacher)
        
        // Set default role to Student
        updateRoleSelection("Student")
    }
    
    private fun setupClickListeners() {
        // Image picker
        btnSelectImage.setOnClickListener {
            openImagePicker()
        }
        
        // Role toggle
        btnStudent.setOnClickListener {
            updateRoleSelection("Student")
        }
        
        btnTeacher.setOnClickListener {
            updateRoleSelection("Teacher")
        }
        
        btnSignup.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            
            if (validateInputs(fullName, email, password, confirmPassword)) {
                signup(fullName, email, password)
            }
        }
        
        txtLogin.setOnClickListener {
            finish() // Go back to login page
        }
    }
    
    private fun openImagePicker() {
        // Check for permission
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, open picker
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                imagePickerLauncher.launch(intent)
            }
            else -> {
                // Request permission
                permissionLauncher.launch(permission)
            }
        }
    }
    
    private fun updateRoleSelection(role: String) {
        selectedRole = role
        
        if (role == "Student") {
            // Student button active
            btnStudent.setBackgroundResource(R.drawable.button_bg)
            btnStudent.setTextColor(Color.WHITE)
            btnStudent.setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Teacher button inactive
            btnTeacher.background = ColorDrawable(Color.TRANSPARENT)
            btnTeacher.setTextColor(Color.parseColor("#666666"))
            btnTeacher.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            // Teacher button active
            btnTeacher.setBackgroundResource(R.drawable.button_bg)
            btnTeacher.setTextColor(Color.WHITE)
            btnTeacher.setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Student button inactive
            btnStudent.background = ColorDrawable(Color.TRANSPARENT)
            btnStudent.setTextColor(Color.parseColor("#666666"))
            btnStudent.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }
    
    private fun validateInputs(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun signup(fullName: String, email: String, password: String) {
        btnSignup.isEnabled = false
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                // Upload image first if selected
                var imageUrl = ""
                if (selectedImageUri != null) {
                    Toast.makeText(this@SignupActivity, "Uploading image...", Toast.LENGTH_SHORT).show()
                    imageUrl = uploadImageToCloudinary(selectedImageUri!!)
                    android.util.Log.d("SignupActivity", "Image URL from upload: $imageUrl")
                }
                
                // Create signup request
                val signupRequest = SignupRequest(
                    fullName = fullName,
                    email = email,
                    password = password,
                    role = selectedRole,
                    profileImage = imageUrl
                )
                android.util.Log.d("SignupActivity", "Sending signup request with profileImage: ${signupRequest.profileImage}")
                
                // Call signup API
                val response = RetrofitClient.getApiService(sessionManager).signup(signupRequest)
                
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string() ?: ""
                    android.util.Log.d("SignupActivity", "Response: $responseBody")
                    
                    // Check if response is valid JSON
                    if (responseBody.trim().startsWith("{") && responseBody.trim().endsWith("}")) {
                        try {
                            val gson = com.google.gson.Gson()
                            val signupResponse = gson.fromJson(responseBody, SignupResponse::class.java)
                            
                            if (signupResponse != null && signupResponse.success) {
                                Toast.makeText(
                                    this@SignupActivity,
                                    "Account created!\nYour Roll Number: ${signupResponse.data?.rollNumber}\nPlease login.",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(this@SignupActivity, signupResponse?.message ?: "Signup failed", Toast.LENGTH_SHORT).show()
                                btnSignup.isEnabled = true
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SignupActivity", "JSON parsing error: ${e.message}", e)
                            Toast.makeText(this@SignupActivity, "Failed to parse response: ${e.message}", Toast.LENGTH_SHORT).show()
                            btnSignup.isEnabled = true
                        }
                    } else {
                        // Response is plain text (error message from server)
                        android.util.Log.e("SignupActivity", "Server returned plain text: $responseBody")
                        Toast.makeText(
                            this@SignupActivity,
                            "Server error: $responseBody",
                            Toast.LENGTH_LONG
                        ).show()
                        btnSignup.isEnabled = true
                    }
                } else {
                    // Handle error response
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when {
                        errorBody?.isNotEmpty() == true -> errorBody
                        response.code() == 409 -> "Email already registered"
                        response.code() == 400 -> "Invalid input data"
                        response.code() == 500 -> "Server error. Please try again later."
                        else -> "Signup failed: ${response.message()}"
                    }
                    Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    btnSignup.isEnabled = true
                }
            } catch (e: Exception) {
                android.util.Log.e("SignupActivity", "Signup failed", e)
                android.util.Log.e("SignupActivity", "Error message: ${e.message}")
                android.util.Log.e("SignupActivity", "Error cause: ${e.cause}")
                e.printStackTrace()
                
                val errorMsg = when (e) {
                    is com.google.gson.JsonSyntaxException -> "Server returned invalid response format"
                    is com.google.gson.stream.MalformedJsonException -> "Server returned malformed JSON"
                    is java.io.IOException -> "Network error. Please check your connection."
                    else -> e.message ?: "Unknown error"
                }
                
                Toast.makeText(
                    this@SignupActivity,
                    "Signup failed: $errorMsg",
                    Toast.LENGTH_LONG
                ).show()
                btnSignup.isEnabled = true
            }
        }
    }
    
    private suspend fun uploadImageToCloudinary(uri: Uri): String {
        return try {
            android.util.Log.d("SignupActivity", "Starting image upload...")
            val inputStream = contentResolver.openInputStream(uri)
            val file = java.io.File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { inputStream?.copyTo(it) }
            
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = okhttp3.MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            // Use PUBLIC endpoint (no auth required)
            android.util.Log.d("SignupActivity", "Calling uploadImagePublic endpoint...")
            val uploadResponse = RetrofitClient.getApiService(sessionManager).uploadImagePublic(body)
            file.delete()
            
            android.util.Log.d("SignupActivity", "Upload response success: ${uploadResponse.success}")
            android.util.Log.d("SignupActivity", "Upload response data: ${uploadResponse.data}")
            android.util.Log.d("SignupActivity", "Upload response URL: ${uploadResponse.data?.url}")
            android.util.Log.d("SignupActivity", "Upload response message: ${uploadResponse.message}")
            
            if (uploadResponse.success && uploadResponse.data?.url != null) {
                android.util.Log.d("SignupActivity", "Returning URL: ${uploadResponse.data.url}")
                uploadResponse.data.url ?: ""
            } else {
                android.util.Log.e("SignupActivity", "Upload failed or no URL returned")
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("SignupActivity", "Image upload exception: ${e.message}")
            android.util.Log.e("SignupActivity", "Exception: ", e)
            ""
        }
    }
}
