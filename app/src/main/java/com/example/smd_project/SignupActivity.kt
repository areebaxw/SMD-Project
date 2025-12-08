package com.example.smd_project

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
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
    private lateinit var btnGenderMale: TextView
    private lateinit var btnGenderFemale: TextView
    private lateinit var studentFieldsContainer: LinearLayout
    private lateinit var spinnerProgram: Spinner
    private lateinit var etDateOfBirth: EditText
    private lateinit var teacherFieldsContainer: LinearLayout
    private lateinit var etPhone: EditText
    private lateinit var spinnerDepartment: Spinner
    private lateinit var spinnerDesignation: Spinner
    private lateinit var etSpecialization: EditText
    
    private var selectedImageUri: Uri? = null
    private var selectedRole: String = "Student" // Default to Student
    private var selectedGender: String = "Male" // Default to Male
    private var selectedProgram: String = ""
    private var selectedDateOfBirth: String = ""
    private var selectedPhone: String = ""
    private var selectedDepartment: String = ""
    private var selectedDesignation: String = ""
    private var selectedSpecialization: String = ""
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                // Compress and display image to avoid "bitmap too large" crash
                loadCompressedImage(uri)
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
        btnGenderMale = findViewById(R.id.btn_gender_male)
        btnGenderFemale = findViewById(R.id.btn_gender_female)
        studentFieldsContainer = findViewById(R.id.student_fields_container)
        spinnerProgram = findViewById(R.id.spinner_program)
        etDateOfBirth = findViewById(R.id.input_date_of_birth)
        teacherFieldsContainer = findViewById(R.id.teacher_fields_container)
        etPhone = findViewById(R.id.input_phone)
        spinnerDepartment = findViewById(R.id.spinner_department)
        spinnerDesignation = findViewById(R.id.spinner_designation)
        etSpecialization = findViewById(R.id.input_specialization)
        
        // Setup program spinner
        val programs = arrayOf("Select Program", "BSCS", "BSAI", "BSCY", "BSDS", "BSSE")
        val programAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, programs)
        programAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProgram.adapter = programAdapter
        
        // Setup department spinner
        val departments = arrayOf(
            "Select Department",
            "Computer Science",
            "Software Engineering",
            "Artificial Intelligence",
            "Cyber Security",
            "Data Science",
            "Information Technology",
            "Mathematics",
            "Physics",
            "English",
            "Management Sciences"
        )
        val departmentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDepartment.adapter = departmentAdapter
        
        // Setup designation spinner
        val designations = arrayOf(
            "Select Designation",
            "Professor",
            "Associate Professor",
            "Assistant Professor",
            "Lecturer",
            "Senior Lecturer",
            "Visiting Faculty",
            "Lab Instructor",
            "Teaching Assistant"
        )
        val designationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, designations)
        designationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDesignation.adapter = designationAdapter
        
        // Set default role to Student
        updateRoleSelection("Student")
        // Set default gender to Male
        updateGenderSelection("Male")
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
        
        // Gender toggle
        btnGenderMale.setOnClickListener {
            updateGenderSelection("Male")
        }
        
        btnGenderFemale.setOnClickListener {
            updateGenderSelection("Female")
        }
        
        // Date picker
        etDateOfBirth.setOnClickListener {
            showDatePicker()
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
            
            // Show student-specific fields
            studentFieldsContainer.visibility = View.VISIBLE
            teacherFieldsContainer.visibility = View.GONE
        } else {
            // Teacher button active
            btnTeacher.setBackgroundResource(R.drawable.button_bg)
            btnTeacher.setTextColor(Color.WHITE)
            btnTeacher.setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Student button inactive
            btnStudent.background = ColorDrawable(Color.TRANSPARENT)
            btnStudent.setTextColor(Color.parseColor("#666666"))
            btnStudent.setTypeface(null, android.graphics.Typeface.NORMAL)
            
            // Hide student fields, show teacher fields
            studentFieldsContainer.visibility = View.GONE
            teacherFieldsContainer.visibility = View.VISIBLE
        }
    }
    
    private fun updateGenderSelection(gender: String) {
        selectedGender = gender
        
        if (gender == "Male") {
            // Male button active
            btnGenderMale.setBackgroundResource(R.drawable.button_bg)
            btnGenderMale.setTextColor(Color.WHITE)
            btnGenderMale.setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Female button inactive
            btnGenderFemale.background = ColorDrawable(Color.TRANSPARENT)
            btnGenderFemale.setTextColor(Color.parseColor("#666666"))
            btnGenderFemale.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            // Female button active
            btnGenderFemale.setBackgroundResource(R.drawable.button_bg)
            btnGenderFemale.setTextColor(Color.WHITE)
            btnGenderFemale.setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Male button inactive
            btnGenderMale.background = ColorDrawable(Color.TRANSPARENT)
            btnGenderMale.setTextColor(Color.parseColor("#666666"))
            btnGenderMale.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format: YYYY-MM-DD
                selectedDateOfBirth = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                etDateOfBirth.setText(selectedDateOfBirth)
            },
            year,
            month,
            day
        )
        
        // Set max date to today (user must be born before today)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        
        datePickerDialog.show()
    }
    
    private fun loadCompressedImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap != null) {
                // Calculate new dimensions (max 800x800 to keep quality but reduce size)
                val maxSize = 800
                val ratio = Math.min(
                    maxSize.toFloat() / originalBitmap.width,
                    maxSize.toFloat() / originalBitmap.height
                )
                
                val newWidth = (originalBitmap.width * ratio).toInt()
                val newHeight = (originalBitmap.height * ratio).toInt()
                
                // Resize bitmap
                val resizedBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    newWidth,
                    newHeight,
                    true
                )
                
                // Display resized bitmap
                ivProfilePicture.setImageBitmap(resizedBitmap)
                
                // Clean up original bitmap if different from resized
                if (resizedBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SignupActivity", "Error loading image: ${e.message}", e)
            Toast.makeText(this, "Failed to load image. Please try another.", Toast.LENGTH_SHORT).show()
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
        
        // Validate student-specific fields
        if (selectedRole == "Student") {
            selectedProgram = spinnerProgram.selectedItem.toString()
            
            if (selectedProgram == "Select Program") {
                Toast.makeText(this, "Please select a program", Toast.LENGTH_SHORT).show()
                return false
            }
            
            if (selectedDateOfBirth.isEmpty()) {
                Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        
        // Validate teacher-specific fields
        if (selectedRole == "Teacher") {
            selectedPhone = etPhone.text.toString().trim()
            selectedDepartment = spinnerDepartment.selectedItem.toString()
            selectedDesignation = spinnerDesignation.selectedItem.toString()
            selectedSpecialization = etSpecialization.text.toString().trim()
            
            if (selectedPhone.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                return false
            }
            
            if (selectedDepartment == "Select Department") {
                Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show()
                return false
            }
            
            if (selectedDesignation == "Select Designation") {
                Toast.makeText(this, "Please select a designation", Toast.LENGTH_SHORT).show()
                return false
            }
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
                    profileImage = imageUrl,
                    gender = selectedGender,
                    program = if (selectedRole == "Student") selectedProgram else null,
                    dateOfBirth = if (selectedRole == "Student") selectedDateOfBirth else null,
                    phone = if (selectedRole == "Teacher") selectedPhone else null,
                    department = if (selectedRole == "Teacher") selectedDepartment else null,
                    designation = if (selectedRole == "Teacher") selectedDesignation else null,
                    specialization = if (selectedRole == "Teacher") selectedSpecialization.ifEmpty { null } else selectedSpecialization
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
            
            // Load and compress the image before uploading
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) {
                android.util.Log.e("SignupActivity", "Failed to decode image")
                return ""
            }
            
            // Resize image to max 1200x1200 for upload (better quality than display)
            val maxSize = 1200
            val ratio = Math.min(
                maxSize.toFloat() / originalBitmap.width,
                maxSize.toFloat() / originalBitmap.height
            )
            
            val newWidth = (originalBitmap.width * ratio).toInt()
            val newHeight = (originalBitmap.height * ratio).toInt()
            
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )
            
            // Compress to JPEG with 85% quality
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // Clean up bitmaps
            if (resizedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()
            
            // Save compressed image to temporary file
            val file = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            fos.write(byteArray)
            fos.close()
            
            android.util.Log.d("SignupActivity", "Compressed image size: ${file.length() / 1024}KB")
            
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
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
