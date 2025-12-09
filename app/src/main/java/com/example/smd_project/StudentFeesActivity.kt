package com.example.smd_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.StudentFeesAdapter
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentFeesActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var rvFees: RecyclerView
    private lateinit var feesAdapter: StudentFeesAdapter
    private lateinit var toolbar: Toolbar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_fees)
        
        sessionManager = SessionManager(this)
        
        // Setup toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Fees"
        
        // Setup RecyclerView
        rvFees = findViewById(R.id.rvFees)
        feesAdapter = StudentFeesAdapter(emptyList())
        rvFees.apply {
            layoutManager = LinearLayoutManager(this@StudentFeesActivity)
            adapter = feesAdapter
        }
        
        loadFeesData()
    }
    
    private fun loadFeesData() {
        val apiService = RetrofitClient.getApiService(sessionManager)
        
        lifecycleScope.launch {
            try {
                val response = apiService.getStudentFees()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val fees = response.body()?.data
                    fees?.let {
                        if (it.fees.isNotEmpty()) {
                            feesAdapter.updateFees(it.fees)
                        } else {
                            Toast.makeText(
                                this@StudentFeesActivity,
                                "No fee records available",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@StudentFeesActivity,
                        response.body()?.message ?: "Failed to load fees",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@StudentFeesActivity,
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
