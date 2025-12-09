package com.example.smd_project

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smd_project.models.CreateEvaluationRequest
import com.example.smd_project.models.EvaluationType
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateEvaluationActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private var courseId: Int = 0
    private var courseName: String = ""
    
    private lateinit var tvCourseName: TextView
    private lateinit var spnEvaluationType: Spinner
    private lateinit var etEvaluationNumber: EditText
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etTotalMarks: EditText
    private lateinit var tvDueDate: TextView
    private lateinit var btnSelectDate: Button
    private lateinit var btnCreateEvaluation: Button
    
    private var evaluationTypes = emptyList<EvaluationType>()
    private var selectedEvaluationTypeId = 0
    private var selectedDueDate: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_evaluation)
        
        sessionManager = SessionManager(this)
        
        courseId = intent.getIntExtra("course_id", 0)
        courseName = intent.getStringExtra("course_name") ?: ""
        
        if (courseId == 0) {
            Toast.makeText(this, "Invalid course", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initViews()
        loadEvaluationTypes()
        setupClickListeners()
    }
    
    private fun initViews() {
        tvCourseName = findViewById(R.id.tvCourseName)
        spnEvaluationType = findViewById(R.id.spnEvaluationType)
        etEvaluationNumber = findViewById(R.id.etEvaluationNumber)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etTotalMarks = findViewById(R.id.etTotalMarks)
        tvDueDate = findViewById(R.id.tvDueDate)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnCreateEvaluation = findViewById(R.id.btnCreateEvaluation)
        
        tvCourseName.text = "Course: $courseName"
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun loadEvaluationTypes() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getEvaluationTypes()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    evaluationTypes = response.body()?.data ?: emptyList()
                    setupEvaluationTypeSpinner()
                } else {
                    Toast.makeText(
                        this@CreateEvaluationActivity,
                        "Failed to load evaluation types",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CreateEvaluationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun setupEvaluationTypeSpinner() {
        val typeNames = evaluationTypes.map { it.evaluation_type_name }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            typeNames
        )
        spnEvaluationType.adapter = adapter
        spnEvaluationType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < evaluationTypes.size) {
                    selectedEvaluationTypeId = evaluationTypes[position].evaluation_type_id
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEvaluationTypeId = 0
            }
        }
    }
    
    private fun setupClickListeners() {
        btnSelectDate.setOnClickListener {
            showDatePicker()
        }
        
        btnCreateEvaluation.setOnClickListener {
            createEvaluation()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDay)
            }
            selectedDueDate = dateFormat.format(date.time)
            tvDueDate.text = "Due Date: $selectedDueDate"
        }, year, month, day).show()
    }
    
    private fun createEvaluation() {
        val evaluationNumber = etEvaluationNumber.text.toString().toIntOrNull()
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val totalMarks = etTotalMarks.text.toString().toIntOrNull()
        
        if (evaluationNumber == null || evaluationNumber <= 0) {
            Toast.makeText(this, "Please enter valid evaluation number", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter evaluation title", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (totalMarks == null || totalMarks <= 0) {
            Toast.makeText(this, "Please enter valid total marks", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedEvaluationTypeId == 0) {
            Toast.makeText(this, "Please select evaluation type", Toast.LENGTH_SHORT).show()
            return
        }
        
        val request = CreateEvaluationRequest(
            courseId = courseId,
            evaluationTypeId = selectedEvaluationTypeId,
            evaluationNumber = evaluationNumber,
            title = title,
            totalMarks = totalMarks
        )
        
        submitCreateEvaluation(request)
    }
    
    private fun submitCreateEvaluation(request: CreateEvaluationRequest) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                btnCreateEvaluation.isEnabled = false
                val response = apiService.createEvaluation(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(
                        this@CreateEvaluationActivity,
                        "Evaluation created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                        this@CreateEvaluationActivity,
                        response.body()?.message ?: "Failed to create evaluation",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnCreateEvaluation.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CreateEvaluationActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                btnCreateEvaluation.isEnabled = true
            }
        }
    }
    
    private fun getCurrentAcademicYear(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        return if (calendar.get(Calendar.MONTH) >= 7) {
            "$year-${year + 1}"
        } else {
            "${year - 1}-$year"
        }
    }
    
    private fun getCurrentSemester(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        return if (month >= 7 || month < 3) "Fall" else "Spring"
    }
}
