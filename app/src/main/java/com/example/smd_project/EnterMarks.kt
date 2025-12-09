package com.example.smd_project

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.EvaluationWithStudentsAdapter
import com.example.smd_project.models.Course
import com.example.smd_project.models.CreateEvaluationRequest
import com.example.smd_project.models.EnterMarksRequest
import com.example.smd_project.models.EvaluationType
import com.example.smd_project.models.EvaluationWithMarks
import com.example.smd_project.models.EvaluationMarksResponse
import com.example.smd_project.models.MarksRecordItem
import com.example.smd_project.models.Student
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class EnterMarks : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var btnSubmit: TextView
    private lateinit var btnSave: TextView
    private lateinit var tvCourseName: TextView
    private lateinit var tvEvaluationType: TextView
    private lateinit var tvEvaluationNumber: TextView
    private lateinit var backArrow: ImageView
    private lateinit var courseDropdownArrow: ImageView
    private lateinit var assignmentDropdownArrow: ImageView
    private lateinit var evaluationNumberDropdownArrow: ImageView
    private lateinit var btnAddEvaluation: TextView
    private lateinit var btnDeleteEvaluation: ImageView
    private lateinit var evaluationDetailsHeader: LinearLayout
    private lateinit var tvSelectedEvalNumber: TextView
    private lateinit var tvSelectedTopic: TextView
    private lateinit var tvSelectedTotalMarks: TextView
    private lateinit var tvSelectedWeightage: TextView
    private lateinit var tvSelectedHighest: TextView
    private lateinit var tvSelectedLowest: TextView
    private lateinit var tvSelectedAverage: TextView
    private lateinit var mainRecyclerView: RecyclerView
    
    private lateinit var evaluationWithStudentsAdapter: EvaluationWithStudentsAdapter
    private val marksMap = mutableMapOf<Int, Double>()
    // Track marks per evaluation number to keep them isolated
    private val marksMapByEvaluation = mutableMapOf<Int, MutableMap<Int, Double>>()
    private var selectedCourseId: Int = 0
    private var selectedEvaluationTypeId: Int = 0
    private var selectedEvaluationNumber: Int = 0
    private var selectedEvaluationDetail: EvaluationWithMarks? = null
    private var courses: List<Course> = emptyList()
    private var evaluationTypes: List<EvaluationType> = emptyList()
    private var allStudents: List<Student> = emptyList()
    private var currentEvaluationRecords: List<EvaluationWithMarks> = emptyList()
    private val enteredMarks = mutableSetOf<Int>() // Track which students have entered marks
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entermarks)
        
        sessionManager = SessionManager(this)
        
        initViews()
        setupRecyclerView()
        loadCoursesAndEvaluationTypes()
        setupClickListeners()
    }
    
    private fun initViews() {
        btnSubmit = findViewById(R.id.btnSubmit)
        btnSave = findViewById(R.id.saveButton)
        tvCourseName = findViewById(R.id.tvCourseName)
        tvEvaluationType = findViewById(R.id.tvEvaluationType)
        tvEvaluationNumber = findViewById(R.id.tvEvaluationNumber)
        btnAddEvaluation = findViewById(R.id.btnAddEvaluation)
        btnDeleteEvaluation = findViewById(R.id.btnDeleteEvaluation)
        evaluationDetailsHeader = findViewById(R.id.evaluationDetailsHeader)
        tvSelectedEvalNumber = findViewById(R.id.tvSelectedEvalNumber)
        tvSelectedTopic = findViewById(R.id.tvSelectedTopic)
        tvSelectedTotalMarks = findViewById(R.id.tvSelectedTotalMarks)
        tvSelectedWeightage = findViewById(R.id.tvSelectedWeightage)
        tvSelectedHighest = findViewById(R.id.tvSelectedHighest)
        tvSelectedLowest = findViewById(R.id.tvSelectedLowest)
        tvSelectedAverage = findViewById(R.id.tvSelectedAverage)
        mainRecyclerView = findViewById(R.id.mainRecyclerView)
        backArrow = findViewById(R.id.backArrow)
        courseDropdownArrow = findViewById(R.id.courseDropdownArrow)
        assignmentDropdownArrow = findViewById(R.id.assignmentDropdownArrow)
        evaluationNumberDropdownArrow = findViewById(R.id.evaluationNumberDropdownArrow)
        
        // Back button
        backArrow.setOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        // Setup adapter for displaying only selected evaluation's students
        evaluationWithStudentsAdapter = EvaluationWithStudentsAdapter(
            evaluations = emptyList(),
            students = allStudents,
            marksMap = marksMap,
            onMarksChange = { studentId, marks ->
                marksMap[studentId] = marks
            },
            onMarksEntered = { studentId, hasValue ->
                if (hasValue) {
                    enteredMarks.add(studentId)
                } else {
                    enteredMarks.remove(studentId)
                }
            }
        )
        
        mainRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@EnterMarks)
            adapter = evaluationWithStudentsAdapter
        }
    }
    
    private fun setupClickListeners() {
        // Save button - saves current marks
        btnSave.setOnClickListener {
            saveMarks()
        }
        
        // Submit button - submits all marks and exits
        btnSubmit.setOnClickListener {
            submitMarks()
        }
        
        // Course dropdown
        courseDropdownArrow.setOnClickListener {
            showCourseDialog()
        }
        tvCourseName.setOnClickListener {
            showCourseDialog()
        }
        
        // Evaluation type dropdown
        assignmentDropdownArrow.setOnClickListener {
            showEvaluationTypeDialog()
        }
        tvEvaluationType.setOnClickListener {
            showEvaluationTypeDialog()
        }
        
        // Evaluation number dropdown
        evaluationNumberDropdownArrow.setOnClickListener {
            showEvaluationNumberDialog()
        }
        tvEvaluationNumber.setOnClickListener {
            showEvaluationNumberDialog()
        }
        
        // Add Evaluation button
        btnAddEvaluation.setOnClickListener {
            showCreateEvaluationDialog()
        }
        
        // Delete Evaluation button
        btnDeleteEvaluation.setOnClickListener {
            deleteSelectedEvaluation()
        }
    }
    
    private fun showCourseDialog() {
        if (courses.isEmpty()) {
            Toast.makeText(this, "No courses available", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val courseNames = courses.map { it.course_name }.filter { it.isNotEmpty() }.toTypedArray()
            
            if (courseNames.isEmpty()) {
                Toast.makeText(this, "No valid courses found", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Create custom dialog with purple Apple UI/UX
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_item, null)
            val tvDialogTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
            val lvItems = dialogView.findViewById<android.widget.ListView>(R.id.lvItems)
            val btnCancel = dialogView.findViewById<android.widget.TextView>(R.id.btnCancelDialog)
            
            tvDialogTitle.text = "Select Course"
            
            val adapter = android.widget.ArrayAdapter(this, R.layout.item_evaluation_dropdown, courseNames)
            lvItems.adapter = adapter
            
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            lvItems.setOnItemClickListener { _, _, which, _ ->
                if (which >= 0 && which < courses.size) {
                    selectedCourseId = courses[which].course_id
                    tvCourseName.text = courses[which].course_name
                    marksMap.clear()
                    tvEvaluationNumber.text = "Select Evaluation"
                    evaluationDetailsHeader.visibility = View.GONE
                    loadStudents(selectedCourseId)
                    dialog.dismiss()
                }
            }
            
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading courses: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun showEvaluationTypeDialog() {
        if (evaluationTypes.isEmpty()) {
            Toast.makeText(this, "Loading evaluation types...", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val evalNames = evaluationTypes.map { it.evaluation_type_name }.filter { it.isNotEmpty() }.toTypedArray()
            
            if (evalNames.isEmpty()) {
                Toast.makeText(this, "No valid evaluation types found", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Create custom dialog with purple Apple UI/UX
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_item, null)
            val tvDialogTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
            val lvItems = dialogView.findViewById<android.widget.ListView>(R.id.lvItems)
            val btnCancel = dialogView.findViewById<android.widget.TextView>(R.id.btnCancelDialog)
            
            tvDialogTitle.text = "Select Evaluation Type"
            
            val adapter = android.widget.ArrayAdapter(this, R.layout.item_evaluation_dropdown, evalNames)
            lvItems.adapter = adapter
            
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            lvItems.setOnItemClickListener { _, _, which, _ ->
                if (which >= 0 && which < evaluationTypes.size) {
                    selectedEvaluationTypeId = evaluationTypes[which].evaluation_type_id
                    tvEvaluationType.text = evaluationTypes[which].evaluation_type_name
                    tvEvaluationNumber.text = "Choose Evaluation"
                    evaluationDetailsHeader.visibility = View.GONE
                    // Reset selected evaluation number so it doesn't try to restore previous selection
                    selectedEvaluationNumber = 0
                    loadExistingMarks(selectedCourseId, selectedEvaluationTypeId)
                    dialog.dismiss()
                }
            }
            
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading evaluation types: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun showEvaluationNumberDialog() {
        if (currentEvaluationRecords.isEmpty()) {
            Toast.makeText(this, "No evaluations found. Load an evaluation type first.", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Remove duplicates and sort
            val uniqueEvaluations = currentEvaluationRecords
                .distinctBy { it.evaluation_number }
                .sortedBy { it.evaluation_number }
            
            val evalNumbers = uniqueEvaluations
                .map { "Evaluation #${it.evaluation_number}: ${it.title}" }
                .toTypedArray()
            
            // Create custom dialog with purple Apple UI/UX
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_evaluation, null)
            val lvEvaluations = dialogView.findViewById<android.widget.ListView>(R.id.lvEvaluations)
            val btnCancel = dialogView.findViewById<android.widget.TextView>(R.id.btnCancelSelectEval)
            
            val adapter = android.widget.ArrayAdapter(this, R.layout.item_evaluation_dropdown, evalNumbers)
            lvEvaluations.adapter = adapter
            
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            lvEvaluations.setOnItemClickListener { _, _, which, _ ->
                if (which >= 0 && which < uniqueEvaluations.size) {
                    val selected = uniqueEvaluations[which]
                    selectedEvaluationNumber = selected.evaluation_number
                    selectedEvaluationDetail = selected
                    tvEvaluationNumber.text = "Evaluation #${selected.evaluation_number}: ${selected.title}"
                    
                    // Clear entered marks for new evaluation
                    enteredMarks.clear()
                    
                    // Show evaluation details header
                    showEvaluationDetails(selected)
                    
                    // Load marks from database for this specific evaluation
                    loadMarksForSpecificEvaluation(selected)
                    
                    dialog.dismiss()
                }
            }
            
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading evaluations: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun showEvaluationDetails(evaluation: EvaluationWithMarks) {
        evaluationDetailsHeader.visibility = View.VISIBLE
        tvSelectedEvalNumber.text = evaluation.evaluation_number.toString()
        tvSelectedTopic.text = evaluation.title
        tvSelectedTotalMarks.text = evaluation.total_marks.toString()
        tvSelectedWeightage.text = evaluation.weightage ?: "-"
        
        // Display statistics if available
        if (evaluation.stats != null) {
            tvSelectedHighest.text = evaluation.stats!!.highest?.toString() ?: "-"
            tvSelectedLowest.text = evaluation.stats!!.lowest?.toString() ?: "-"
            tvSelectedAverage.text = evaluation.stats!!.average?.toString() ?: "-"
        } else {
            tvSelectedHighest.text = "-"
            tvSelectedLowest.text = "-"
            tvSelectedAverage.text = "-"
        }
    }
    
    private fun updateStudentListForSelectedEvaluation(evaluation: EvaluationWithMarks) {
        // Update evaluation data FIRST with new data
        val evaluationList = listOf(evaluation)
        evaluationWithStudentsAdapter.updateData(evaluationList, allStudents)
        
        // Then update marks and refresh - this ensures marks are available when adapter binds
        evaluationWithStudentsAdapter.updateMarksMapAndRefresh(marksMap)
    }
    
    private fun showCreateEvaluationDialog() {
        // First, ask for evaluation type
        if (evaluationTypes.isEmpty()) {
            Toast.makeText(this, "Loading evaluation types...", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val evalTypeNames = evaluationTypes.map { it.evaluation_type_name }.filter { it.isNotEmpty() }.toTypedArray()
            
            if (evalTypeNames.isEmpty()) {
                Toast.makeText(this, "No evaluation types available", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Create custom dialog with purple Apple UI/UX
            val dialogView = layoutInflater.inflate(R.layout.dialog_select_eval_type, null)
            val lvEvalTypes = dialogView.findViewById<android.widget.ListView>(R.id.lvEvalTypes)
            val btnCancel = dialogView.findViewById<android.widget.TextView>(R.id.btnCancelSelectType)
            
            val adapter = android.widget.ArrayAdapter(this, R.layout.item_evaluation_dropdown, evalTypeNames)
            lvEvalTypes.adapter = adapter
            
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            lvEvalTypes.setOnItemClickListener { _, _, which, _ ->
                if (which >= 0 && which < evaluationTypes.size) {
                    selectedEvaluationTypeId = evaluationTypes[which].evaluation_type_id
                    tvEvaluationType.text = evaluationTypes[which].evaluation_type_name
                    dialog.dismiss()
                    // Now show the form to enter evaluation details
                    showEvaluationDetailsForm()
                }
            }
            
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun showEvaluationDetailsForm() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_evaluation, null)
        
        val etEvaluationNum = dialogView.findViewById<EditText>(R.id.etEvaluationNum)
        val etTotalMarks = dialogView.findViewById<EditText>(R.id.etTotalMarks)
        val etWeightage = dialogView.findViewById<EditText>(R.id.etWeightage)
        val etTopic = dialogView.findViewById<EditText>(R.id.etTopic)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancelEvalDialog)
        val btnCreate = dialogView.findViewById<TextView>(R.id.btnCreateEval)
        
        // Set evaluation number to 0 by default
        etEvaluationNum.setText("0")
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnCreate.setOnClickListener {
            val evalNum = etEvaluationNum.text.toString().trim().toIntOrNull()
            val totalMarks = etTotalMarks.text.toString().trim().toIntOrNull()
            val weightage = etWeightage.text.toString().trim()
            val topic = etTopic.text.toString().trim()
            
            if (evalNum == null || totalMarks == null || topic.isEmpty()) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Create evaluation on backend
            createEvaluationOnBackend(evalNum, totalMarks, weightage, topic, dialog)
        }
        
        dialog.show()
    }
    
    private fun createEvaluationOnBackend(evalNum: Int, totalMarks: Int, weightage: String, topic: String, dialog: AlertDialog) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                Toast.makeText(this@EnterMarks, "Creating evaluation #$evalNum...", Toast.LENGTH_SHORT).show()
                
                // Create request object with weightage
                val createEvalRequest = CreateEvaluationRequest(
                    courseId = selectedCourseId,
                    evaluationTypeId = selectedEvaluationTypeId,
                    evaluationNumber = evalNum,
                    title = topic,
                    totalMarks = totalMarks,
                    weightage = weightage.ifEmpty { null }
                )
                
                // Call API to create evaluation
                val response = apiService.createEvaluation(createEvalRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val createdEvalId = response.body()?.data?.evaluationId ?: 0
                    
                    // Create evaluation object for display with weightage
                    val newEvaluation = EvaluationWithMarks(
                        evaluation_id = createdEvalId,
                        evaluation_number = evalNum,
                        title = topic,
                        total_marks = totalMarks,
                        academic_year = "",
                        semester = "",
                        marks = emptyList(),
                        weightage = weightage.ifEmpty { "-" }
                    )
                    
                    selectedEvaluationNumber = evalNum
                    selectedEvaluationDetail = newEvaluation
                    
                    // Add to current evaluations
                    currentEvaluationRecords = currentEvaluationRecords + newEvaluation
                    
                    // Hide the form dialog
                    dialog.dismiss()
                    
                    // Show the evaluation details and redirect to it
                    showEvaluationDetails(newEvaluation)
                    updateStudentListForSelectedEvaluation(newEvaluation)
                    
                    Toast.makeText(this@EnterMarks, "Evaluation #$evalNum created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EnterMarks, "Failed to create evaluation", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnterMarks, "Error creating evaluation: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun loadCoursesAndEvaluationTypes() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                // Load courses
                try {
                    val coursesResponse = apiService.getTeacherCourses()
                    if (coursesResponse.isSuccessful) {
                        val body = coursesResponse.body()
                        if (body?.success == true && !body.data.isNullOrEmpty()) {
                            courses = body.data.filter { 
                                it.course_id > 0 && it.course_name.isNotEmpty() 
                            }
                            
                            if (courses.isNotEmpty()) {
                                selectedCourseId = courses[0].course_id
                                tvCourseName.text = courses[0].course_name
                                loadStudents(courses[0].course_id)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                // Load evaluation types
                try {
                    val evalResponse = apiService.getEvaluationTypes()
                    if (evalResponse.isSuccessful) {
                        val body = evalResponse.body()
                        if (body?.success == true && !body.data.isNullOrEmpty()) {
                            evaluationTypes = body.data.filter { 
                                it.evaluation_type_id > 0 && it.evaluation_type_name.isNotEmpty() 
                            }
                            
                            if (evaluationTypes.isNotEmpty()) {
                                selectedEvaluationTypeId = evaluationTypes[0].evaluation_type_id
                                tvEvaluationType.text = evaluationTypes[0].evaluation_type_name
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnterMarks, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun loadStudents(courseId: Int) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getCourseStudents(courseId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val students = response.body()?.data ?: emptyList()
                    allStudents = students
                    
                    // Initialize marks map with 0
                    students.forEach { student ->
                        if (!marksMap.containsKey(student.student_id)) {
                            marksMap[student.student_id] = 0.0
                        }
                    }
                    
                    // Display all students immediately
                    displayAllStudents(students)
                } else {
                    Toast.makeText(this@EnterMarks,
                        "Failed to load students",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnterMarks, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun displayAllStudents(students: List<Student>) {
        // Hide evaluation details header when no evaluation is selected
        evaluationDetailsHeader.visibility = View.GONE
        
        // Clear marks and reset to all zeros when showing all students
        // This ensures we don't see marks from previous evaluations
        marksMap.clear()
        for (student in students) {
            marksMap[student.student_id] = 0.0
        }
        
        // Create empty evaluation with all students for display
        val allStudentsEval = EvaluationWithMarks(
            evaluation_id = 0,
            evaluation_number = 0,
            title = "",
            total_marks = 0,
            academic_year = "",
            semester = "",
            marks = emptyList()
        )
        
        evaluationWithStudentsAdapter.updateData(listOf(allStudentsEval), students)
    }
    
    private fun loadMarksForSpecificEvaluation(evaluation: EvaluationWithMarks) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                // Load marks for this specific evaluation number
                val response = apiService.getEvaluationMarks(selectedCourseId, selectedEvaluationTypeId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        // Find this specific evaluation in the response
                        val selectedEval = data.evaluations.find { it.evaluation_number == evaluation.evaluation_number }
                        
                        if (selectedEval != null) {
                            // Clear and load marks for only this evaluation
                            marksMap.clear()
                            
                            // Load marks from database for this evaluation
                            for (mark in selectedEval.marks) {
                                val marks = when (mark.obtained_marks) {
                                    is String -> (mark.obtained_marks as String).toDoubleOrNull() ?: 0.0
                                    is Double -> mark.obtained_marks as Double
                                    is Int -> (mark.obtained_marks as Int).toDouble()
                                    else -> 0.0
                                }
                                marksMap[mark.student_id] = marks
                            }
                            
                            // For any student without marks in this evaluation, initialize to 0
                            for (student in data.students) {
                                if (!marksMap.containsKey(student.student_id)) {
                                    marksMap[student.student_id] = 0.0
                                }
                            }
                            
                            android.util.Log.d("EnterMarks", "Loaded eval #${evaluation.evaluation_number} from DB: ${marksMap.size} marks - $marksMap")
                            
                            // Update adapter with fresh data from database
                            updateStudentListForSelectedEvaluation(selectedEval)
                            enteredMarks.clear()
                        }
                    }
                } else {
                    Toast.makeText(this@EnterMarks, "Failed to load marks", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnterMarks, "Error loading marks: ${e.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.e("EnterMarks", "Error loading marks for eval #${evaluation.evaluation_number}: ${e.message}", e)
            }
        }
    }
    
    private fun loadExistingMarks(courseId: Int, evaluationTypeId: Int) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getEvaluationMarks(courseId, evaluationTypeId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        currentEvaluationRecords = data.evaluations
                        
                        // Clear and rebuild marks maps from database
                        marksMap.clear()
                        marksMapByEvaluation.clear()
                        
                        // Initialize marks for each evaluation separately
                        for (evaluation in data.evaluations) {
                            val evalSpecificMarks = mutableMapOf<Int, Double>()
                            
                            // Load marks from database for this evaluation
                            for (mark in evaluation.marks) {
                                evalSpecificMarks[mark.student_id] = mark.obtained_marks ?: 0.0
                            }
                            
                            // For any student without marks in this evaluation, initialize to 0
                            for (student in data.students) {
                                if (!evalSpecificMarks.containsKey(student.student_id)) {
                                    evalSpecificMarks[student.student_id] = 0.0
                                }
                            }
                            
                            // Store this evaluation's marks separately
                            marksMapByEvaluation[evaluation.evaluation_number] = evalSpecificMarks
                            
                            // Debug logging
                            android.util.Log.d("EnterMarks", "Loaded eval #${evaluation.evaluation_number}: ${evalSpecificMarks.size} marks - $evalSpecificMarks")
                        }
                        
                        // If a specific evaluation was previously selected, keep it displayed
                        if (selectedEvaluationNumber > 0) {
                            val selected = currentEvaluationRecords.find { it.evaluation_number == selectedEvaluationNumber }
                            if (selected != null) {
                                // Restore the selected evaluation display with its specific marks
                                marksMap.clear()
                                marksMap.putAll(marksMapByEvaluation[selectedEvaluationNumber] ?: emptyMap())
                                android.util.Log.d("EnterMarks", "Restored eval #$selectedEvaluationNumber marks: $marksMap")
                                showEvaluationDetails(selected)
                                updateStudentListForSelectedEvaluation(selected)
                                enteredMarks.clear()
                            } else {
                                // If not found, display all students
                                displayMarksWithEvaluations(data)
                            }
                        } else {
                            // Display all students with their marks organized by evaluation
                            displayMarksWithEvaluations(data)
                        }
                    }
                } else {
                    // No data found, clear marks
                    marksMap.clear()
                    marksMapByEvaluation.clear()
                    currentEvaluationRecords = emptyList()
                    tvEvaluationNumber.text = "Select Evaluation"
                    Toast.makeText(this@EnterMarks, "No previous marks found. Start fresh.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // If error, just continue with empty marks
                marksMap.clear()
                marksMapByEvaluation.clear()
                currentEvaluationRecords = emptyList()
                tvEvaluationNumber.text = "Select Evaluation"
                android.util.Log.e("EnterMarks", "Error loading marks: ${e.message}", e)
                Toast.makeText(this@EnterMarks, "Error loading marks: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun displayMarksWithEvaluations(data: EvaluationMarksResponse) {
        if (data.students.isEmpty()) {
            evaluationWithStudentsAdapter.updateData(emptyList(), emptyList())
            evaluationDetailsHeader.visibility = View.GONE
            Toast.makeText(this, "No students found for this course", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Hide evaluation details header when showing all students
        evaluationDetailsHeader.visibility = View.GONE
        
        // When no specific evaluation is selected, clear marks and show all students with 0 marks
        // This ensures we don't see marks from other evaluations
        marksMap.clear()
        for (student in data.students) {
            marksMap[student.student_id] = 0.0
        }
        
        // Create a single evaluation container to show all students without grouping
        val allStudentsEval = EvaluationWithMarks(
            evaluation_id = 0,
            evaluation_number = 0,
            title = "",
            total_marks = 0,
            academic_year = "",
            semester = "",
            marks = emptyList()
        )
        
        // Update adapter with single evaluation showing all students
        // This displays all students in a flat list without evaluation grouping
        evaluationWithStudentsAdapter.updateData(
            listOf(allStudentsEval),
            data.students
        )
    }
    
    private fun saveMarks() {
        // Marks are automatically saved to the database when submitted
        // This button is for user confirmation
        Toast.makeText(this, "Marks will be saved to database when you submit", Toast.LENGTH_SHORT).show()
    }
    
    private fun submitMarks() {
        if (selectedCourseId == 0) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedEvaluationTypeId == 0) {
            Toast.makeText(this, "Please select evaluation type", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedEvaluationNumber == 0) {
            Toast.makeText(this, "Please select an evaluation", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get the selected evaluation details
        val selected = currentEvaluationRecords.find { it.evaluation_number == selectedEvaluationNumber }
        if (selected == null) {
            Toast.makeText(this, "Selected evaluation not found", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get all marks that have been entered (only those with actual values)
        val marksRecords = enteredMarks.map { studentId ->
            MarksRecordItem(studentId, marksMap[studentId] ?: 0.0)
        }
        
        if (marksRecords.isEmpty()) {
            Toast.makeText(this, "Please enter marks for at least one student", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show confirmation dialog with purple Apple UI/UX
        showConfirmationDialog(selected, marksRecords)
    }
    
    private fun showConfirmationDialog(selected: EvaluationWithMarks, marksRecords: List<MarksRecordItem>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_marks, null)
        
        val tvConfirmTitle = dialogView.findViewById<TextView>(R.id.tvConfirmTitle)
        val tvConfirmMessage = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancelConfirm)
        
        tvConfirmTitle.text = "Confirm Mark Update"
        tvConfirmMessage.text = "Evaluation #${selected.evaluation_number}: ${selected.title}\n" +
            "Total Marks: ${selected.total_marks}\n" +
            "Students: ${marksRecords.size}\n\n" +
            "Update these marks?"
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            performSubmit(marksRecords, selected.evaluation_number, selected.title, selected.total_marks)
        }
        
        dialog.show()
    }
    
    private fun performSubmit(marksRecords: List<MarksRecordItem>, evaluationNumber: Int, title: String, totalMarks: Int) {
        val request = EnterMarksRequest(
            course_id = selectedCourseId,
            evaluation_type_id = selectedEvaluationTypeId,
            evaluation_number = evaluationNumber,
            title = title,
            total_marks = totalMarks,
            academic_year = "2025",
            semester = "Fall",
            marks_records = marksRecords
        )
        
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.enterMarks(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EnterMarks,
                        "✓ Marks updated successfully",
                        Toast.LENGTH_SHORT).show()
                    
                    // Update local cache with newly submitted marks
                    // This way we don't need to reload from server
                    val updatedMarks = mutableMapOf<Int, Double>()
                    
                    // First, get existing marks for this evaluation
                    val existingMarks = marksMapByEvaluation[evaluationNumber] ?: mutableMapOf()
                    updatedMarks.putAll(existingMarks)
                    
                    // Then update with newly submitted marks
                    for (record in marksRecords) {
                        updatedMarks[record.student_id] = record.obtained_marks
                    }
                    
                    // Store updated marks back
                    marksMapByEvaluation[evaluationNumber] = updatedMarks
                    
                    // Update current marksMap to reflect changes
                    marksMap.clear()
                    marksMap.putAll(updatedMarks)
                    
                    // Refresh the adapter display without reloading from server
                    updateStudentListForSelectedEvaluation(selectedEvaluationDetail ?: return@launch)
                    
                    // Clear entered marks for next submission
                    enteredMarks.clear()
                    
                    android.util.Log.d("EnterMarks", "Updated eval #$evaluationNumber locally with marks: $updatedMarks")
                } else {
                    Toast.makeText(this@EnterMarks,
                        response.body()?.message ?: "Failed to submit marks",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnterMarks, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun deleteSelectedEvaluation() {
        if (selectedEvaluationNumber == 0) {
            Toast.makeText(this, "Please select an evaluation to delete", Toast.LENGTH_SHORT).show()
            return
        }
        
        val selected = selectedEvaluationDetail
        if (selected == null) {
            Toast.makeText(this, "Evaluation not found", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show confirmation dialog with purple Apple UI/UX
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_marks, null)
        val tvConfirmTitle = dialogView.findViewById<TextView>(R.id.tvConfirmTitle)
        val tvConfirmMessage = dialogView.findViewById<TextView>(R.id.tvConfirmMessage)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancelConfirm)
        
        tvConfirmTitle.text = "Delete Evaluation?"
        tvConfirmMessage.text = "Evaluation #${selected.evaluation_number}: ${selected.title}\n" +
            "Total Marks: ${selected.total_marks}\n\n" +
            "This action cannot be undone. All marks for this evaluation will be deleted."
        
        btnConfirm.text = "Delete"
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            performDelete(selected.evaluation_id, selected.evaluation_number)
        }
        
        dialog.show()
    }
    
    private fun performDelete(evaluationId: Int, evaluationNumber: Int) {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.deleteEvaluation(evaluationId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EnterMarks,
                        "✓ Evaluation deleted successfully",
                        Toast.LENGTH_SHORT).show()
                    
                    // Clear the deleted evaluation from tracking
                    marksMapByEvaluation.remove(evaluationNumber)
                    selectedEvaluationNumber = 0
                    selectedEvaluationDetail = null
                    tvEvaluationNumber.text = "Select Evaluation"
                    evaluationDetailsHeader.visibility = View.GONE
                    enteredMarks.clear()
                    marksMap.clear()
                    
                    // Reload existing marks to show updated list
                    loadExistingMarks(selectedCourseId, selectedEvaluationTypeId)
                } else {
                    Toast.makeText(this@EnterMarks,
                        response.body()?.message ?: "Failed to delete evaluation",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnterMarks, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
