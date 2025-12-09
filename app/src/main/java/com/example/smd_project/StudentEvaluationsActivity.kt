package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.CourseEvaluationsAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentEvaluationsActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvEvaluations: RecyclerView
    private lateinit var evaluationsAdapter: CourseEvaluationsAdapter
    private lateinit var toolbar: Toolbar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_evaluations)
        
        sessionManager = SessionManager(this)
        
        // Setup toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Evaluations"
        
        // Setup RecyclerView
        rvEvaluations = findViewById(R.id.rvEvaluations)
        evaluationsAdapter = CourseEvaluationsAdapter(emptyList())
        rvEvaluations.apply {
            layoutManager = LinearLayoutManager(this@StudentEvaluationsActivity)
            adapter = evaluationsAdapter
        }
        
        loadEvaluationsData()
    }
    
    private fun loadEvaluationsData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStudentEvaluations()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val evaluations = response.body()?.data
                    evaluations?.let {
                        if (it.isNotEmpty()) {
                            evaluationsAdapter.updateEvaluations(it)
                        } else {
                            Toast.makeText(
                                this@StudentEvaluationsActivity,
                                "No evaluations available for your courses",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to load evaluations"
                    Toast.makeText(
                        this@StudentEvaluationsActivity,
                        errorMsg,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@StudentEvaluationsActivity,
                    "Error loading evaluations: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
