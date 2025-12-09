package com.example.smd_project

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.StudentMarkDisplayAdapter
import com.example.smd_project.models.Mark
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentMarksActivity : AppCompatActivity() {
    
    private companion object {
        const val TAG = "StudentMarksActivity"
    }
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvMarks: RecyclerView
    private lateinit var marksAdapter: StudentMarkDisplayAdapter
    private lateinit var toolbar: Toolbar
    
    private var allMarks: List<Mark> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_marks)
        
        sessionManager = SessionManager(this)
        
        // Setup toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Evaluations"
        
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
                Log.d(TAG, "Starting to fetch marks...")
                val response = apiService.getStudentMarks()
                
                Log.d(TAG, "Response received - Code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    try {
                        val body = response.body()
                        Log.d(TAG, "Response body: $body")
                        
                        if (body?.success == true) {
                            val marksList = body.data
                            Log.d(TAG, "Marks response: $marksList")
                            
                            marksList?.let {
                                Log.d(TAG, "Number of marks: ${it.size}")
                                if (it.isNotEmpty()) {
                                    allMarks = it
                                    Log.d(TAG, "Loaded ${allMarks.size} marks")
                                    marksAdapter.updateMarks(it)
                                } else {
                                    Log.d(TAG, "No marks available")
                                    Toast.makeText(
                                        this@StudentMarksActivity,
                                        "No evaluations available yet",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Log.e(TAG, "Success flag is false: ${body?.message}")
                            Toast.makeText(
                                this@StudentMarksActivity,
                                body?.message ?: "Failed to load evaluations",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (parseError: Exception) {
                        Log.e(TAG, "Failed to parse response body", parseError)
                        Toast.makeText(
                            this@StudentMarksActivity,
                            "Error parsing server response: ${parseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Request failed: ${response.code()}, error: $errorBody")
                    Toast.makeText(
                        this@StudentMarksActivity,
                        "Server error: ${response.code()} - Please check logs",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred", e)
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
