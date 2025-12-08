package com.example.smd_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.EvaluationAdapter
import com.example.smd_project.models.Evaluation
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class EvaluationListActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private var courseId: Int = 0
    private var courseName: String = ""
    
    private lateinit var tvCourseName: TextView
    private lateinit var rvEvaluations: RecyclerView
    private lateinit var btnCreateEvaluation: Button
    private lateinit var evaluationAdapter: EvaluationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluation_list)
        
        sessionManager = SessionManager(this)
        
        courseId = intent.getIntExtra("course_id", 0)
        courseName = intent.getStringExtra("course_name") ?: ""
        
        if (courseId == 0) {
            Toast.makeText(this, "Invalid course", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadEvaluations()
    }
    
    private fun initViews() {
        tvCourseName = findViewById(R.id.tvCourseName)
        rvEvaluations = findViewById(R.id.rvEvaluations)
        btnCreateEvaluation = findViewById(R.id.btnCreateEvaluation)
        
        tvCourseName.text = courseName
        
        // Setup back button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { finish() }
        }
    }
    
    private fun setupRecyclerView() {
        evaluationAdapter = EvaluationAdapter(emptyList()) { evaluation ->
            navigateToMarkMarks(evaluation)
        }
        rvEvaluations.apply {
            layoutManager = LinearLayoutManager(this@EvaluationListActivity)
            adapter = evaluationAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnCreateEvaluation.setOnClickListener {
            val intent = Intent(this, CreateEvaluationActivity::class.java).apply {
                putExtra("course_id", courseId)
                putExtra("course_name", courseName)
            }
            startActivity(intent)
        }
    }
    
    private fun loadEvaluations() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getCourseEvaluations(courseId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val evaluations = response.body()?.data ?: emptyList()
                    evaluationAdapter.updateEvaluations(evaluations)
                } else {
                    Toast.makeText(
                        this@EvaluationListActivity,
                        "Failed to load evaluations",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@EvaluationListActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun navigateToMarkMarks(evaluation: Evaluation) {
        val intent = Intent(this, MarkStudentsActivity::class.java).apply {
            putExtra("evaluation_id", evaluation.evaluation_id)
            putExtra("course_id", courseId)
            putExtra("evaluation_title", evaluation.title)
            putExtra("total_marks", evaluation.total_marks)
        }
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        loadEvaluations()
    }
}
