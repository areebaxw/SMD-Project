package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.StudentMarkDisplayAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentMarksActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvMarks: RecyclerView
    private lateinit var marksAdapter: StudentMarkDisplayAdapter
    private lateinit var toolbar: Toolbar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_marks)
        
        sessionManager = SessionManager(this)
        
        // Setup toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Marks"
        
        // Setup RecyclerView
        rvMarks = findViewById(R.id.rvMarks)
        marksAdapter = StudentMarkDisplayAdapter(emptyList())
        rvMarks.apply {
            layoutManager = LinearLayoutManager(this@StudentMarksActivity)
            adapter = marksAdapter
        }
        
        loadMarksData()
    }
    
    private fun loadMarksData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStudentMarks()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val marksResponse = response.body()?.data
                    marksResponse?.let {
                        if (it.marks.isNotEmpty()) {
                            marksAdapter.updateMarks(it.marks)
                        } else {
                            Toast.makeText(
                                this@StudentMarksActivity,
                                "No marks available yet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@StudentMarksActivity,
                        response.body()?.message ?: "Failed to load marks",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@StudentMarksActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
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
