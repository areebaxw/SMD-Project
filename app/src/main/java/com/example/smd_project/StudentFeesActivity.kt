package com.example.smd_project

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.adapters.StudentFeesAdapter
import com.example.smd_project.models.Fee
import com.example.smd_project.models.StudentCourse
import com.example.smd_project.models.StudentFeeItem
import com.example.smd_project.models.UpdateTotalFeeRequest
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch

class StudentFeesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var rvFees: RecyclerView
    private lateinit var feesAdapter: StudentFeesAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var btnPrintFees: Button

    companion object {
        const val CREDIT_COST = 11000 // Fee per credit hour
        const val TAG = "StudentFeesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_fees)

        sessionManager = SessionManager(this)

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Fees"

        // RecyclerView setup
        rvFees = findViewById(R.id.rvFees)
        feesAdapter = StudentFeesAdapter(emptyList(), sessionManager)
        rvFees.apply {
            layoutManager = LinearLayoutManager(this@StudentFeesActivity)
            adapter = feesAdapter
        }

        // Add Print Button programmatically at the bottom
        btnPrintFees = Button(this).apply {
            text = "Print Fees"
            setOnClickListener { printFees() }
        }

        // Add button to layout
        val parentLayout = findViewById<LinearLayout>(R.id.rootLayout)
        parentLayout.addView(btnPrintFees)

        Log.d(TAG, "onCreate: Starting to load fees data")
        loadFeesData()
    }

    private fun loadFeesData() {
        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            try {
                // Fetch enrolled courses
                Log.d(TAG, "Fetching enrolled courses")
                val coursesResponse = apiService.getStudentEnrolledCourses()
                if (coursesResponse.isSuccessful && coursesResponse.body()?.success == true) {
                    val coursesList: List<StudentCourse> = coursesResponse.body()?.data ?: emptyList()
                    Log.d(TAG, "Enrolled courses fetched: ${coursesList.size} courses")

                    if (coursesList.isNotEmpty()) {
                        val totalCredits = coursesList.sumOf { it.credit_hours }
                        Log.d(TAG, "Total credits: $totalCredits")
                        val calculatedTotalAmount = totalCredits * CREDIT_COST.toDouble()
                        Log.d(TAG, "Calculated total amount: $calculatedTotalAmount")

                        // Update total amount in DB
                        val updateResponse = updateTotalAmountInDB(calculatedTotalAmount)
                        Log.d(TAG, "Total amount update response: $updateResponse")
                        if (!updateResponse) {
                            Toast.makeText(
                                this@StudentFeesActivity,
                                "Failed to update total fee in DB",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // Fetch existing fees
                        Log.d(TAG, "Fetching existing fees")
                        val feesResponse = apiService.getStudentFees()
                        val existingFees: List<StudentFeeItem> = if (feesResponse.isSuccessful && feesResponse.body()?.success == true) {
                            feesResponse.body()?.data ?: emptyList()
                        } else emptyList()
                        Log.d(TAG, "Existing fees fetched: ${existingFees.size}")

                        val mappedFees = existingFees.map { fee ->
                            Log.d(TAG, "Mapping fee: $fee")
                            Fee(
                                fee_id = fee.fee_id,
                                student_id = fee.student_id,
                                program = "Enrolled Courses",
                                semester = fee.semester,
                                academic_year = fee.academic_year ?: "",
                                total_amount = calculatedTotalAmount,
                                paid_amount = fee.paid_amount ?: 0.0,
                                remaining_amount = calculatedTotalAmount - (fee.paid_amount ?: 0.0),
                                payment_status = when {
                                    (fee.paid_amount ?: 0.0) >= calculatedTotalAmount -> "Paid"
                                    (fee.paid_amount ?: 0.0) > 0 -> "Partial"
                                    else -> "Pending"
                                },
                                due_date = fee.due_date
                            )
                        }.ifEmpty {
                            listOf(
                                Fee(
                                    fee_id = 0,
                                    student_id = sessionManager.getUserId(),
                                    program = "Enrolled Courses",
                                    semester = 0,
                                    academic_year = "",
                                    total_amount = calculatedTotalAmount,
                                    paid_amount = 0.0,
                                    remaining_amount = calculatedTotalAmount,
                                    payment_status = "Pending",
                                    due_date = null
                                )
                            )
                        }

                        Log.d(TAG, "Mapped fees size: ${mappedFees.size}")
                        feesAdapter.updateFees(mappedFees)
                        Log.d(TAG, "Fees adapter updated")

                    } else {
                        Log.d(TAG, "No courses enrolled yet")
                        Toast.makeText(
                            this@StudentFeesActivity,
                            "No courses enrolled yet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Log.e(TAG, "Failed to fetch courses: ${coursesResponse.code()} ${coursesResponse.message()}")
                    Toast.makeText(
                        this@StudentFeesActivity,
                        "Failed to fetch courses",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading fees data", e)
                Toast.makeText(
                    this@StudentFeesActivity,
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Update total amount in database
    private suspend fun updateTotalAmountInDB(totalAmount: Double): Boolean {
        val apiService = RetrofitClient.getApiService(sessionManager)
        return try {
            val request = UpdateTotalFeeRequest(
                student_id = sessionManager.getUserId(),
                total_amount = totalAmount
            )
            val response = apiService.updateStudentFeeTotal(request)
            Log.d(TAG, "updateTotalAmountInDB response: ${response.body()}")
            response.isSuccessful && (response.body()?.success == true)
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating total fee in DB", e)
            false
        }
    }

    // Print fees
    private fun printFees() {
        if (feesAdapter.itemCount == 0) {
            Toast.makeText(this, "No fees to print", Toast.LENGTH_SHORT).show()
            return
        }

        val printManager = getSystemService(PRINT_SERVICE) as android.print.PrintManager
        val jobName = "${getString(R.string.app_name)} Fees"

        val adapter = object : android.print.PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: android.print.PrintAttributes?,
                newAttributes: android.print.PrintAttributes,
                cancellationSignal: android.os.CancellationSignal?,
                callback: android.print.PrintDocumentAdapter.LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = android.print.PrintDocumentInfo.Builder("fees_report.pdf")
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor,
                cancellationSignal: android.os.CancellationSignal?,
                callback: android.print.PrintDocumentAdapter.WriteResultCallback?
            ) {
                try {
                    val outputStream = android.os.ParcelFileDescriptor.AutoCloseOutputStream(destination)
                    val feesText = buildFeesText()
                    outputStream.write(feesText.toByteArray())
                    outputStream.close()
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(jobName, adapter, null)
    }

    private fun buildFeesText(): String {
        val builder = StringBuilder()
        builder.append("Student Fees Report\n\n")
        for (i in 0 until feesAdapter.itemCount) {
            val fee = feesAdapter.getFeeAt(i)
            builder.append("Program: ${fee.program}\n")
            builder.append("Semester: ${fee.semester} - ${fee.academic_year}\n")
            builder.append("Total: PKR ${fee.total_amount}\n")
            builder.append("Paid: PKR ${fee.paid_amount}\n")
            builder.append("Remaining: PKR ${fee.remaining_amount}\n")
            builder.append("Status: ${fee.payment_status}\n")
            builder.append("Due Date: ${fee.due_date ?: "N/A"}\n")
            builder.append("-----------------------------\n")
        }
        return builder.toString()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
