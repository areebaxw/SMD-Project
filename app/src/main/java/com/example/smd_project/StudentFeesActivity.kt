package com.example.smd_project

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.smd_project.adapters.StudentFeesAdapter
import com.example.smd_project.models.Fee
import com.example.smd_project.models.StudentCourse
import com.example.smd_project.models.StudentFeeItem
import com.example.smd_project.models.UpdateTotalFeeRequest
import com.example.smd_project.models.PaymentHistoryItem
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.repository.StudentRepository
import com.example.smd_project.utils.NetworkUtils
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class StudentFeesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var repository: StudentRepository
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var rvFees: RecyclerView
    private lateinit var feesAdapter: StudentFeesAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var btnPrintFees: Button
    private lateinit var llPaymentHistoryContainer: LinearLayout

    companion object {
        const val CREDIT_COST = 11000 // Fee per credit hour
        const val TAG = "StudentFeesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_fees)

        sessionManager = SessionManager(this)
        repository = StudentRepository(this)

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Make back arrow white
        val backArrow = toolbar.navigationIcon
        if (backArrow != null) {
            backArrow.setTint(android.graphics.Color.WHITE)
            toolbar.navigationIcon = backArrow
        }

        // RecyclerView setup
        rvFees = findViewById(R.id.rvFees)
        feesAdapter = StudentFeesAdapter(emptyList(), sessionManager)
        rvFees.apply {
            layoutManager = LinearLayoutManager(this@StudentFeesActivity)
            adapter = feesAdapter
        }

        // Payment history container
        llPaymentHistoryContainer = findViewById(R.id.llPaymentHistoryContainer)

        // Print Button
        btnPrintFees = findViewById(R.id.btnPrintFees)
        btnPrintFees.setOnClickListener { printFees() }

        // Setup swipe refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        setupSwipeRefresh()
        
        // Load fees and their payment histories
        observeFees()
    }
    
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            if (NetworkUtils.isOnline(this)) {
                lifecycleScope.launch {
                    repository.refreshFees()
                    swipeRefreshLayout.isRefreshing = false
                }
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun observeFees() {
        repository.getFees().observe(this) { feeEntities ->
            val fees = feeEntities.map { entity ->
                Fee(
                    fee_id = entity.fee_id,
                    student_id = entity.student_id,
                    program = entity.program ?: "N/A",
                    semester = entity.semester ?: 0,
                    academic_year = entity.academic_year ?: "",
                    total_amount = entity.total_amount,
                    paid_amount = entity.paid_amount,
                    remaining_amount = entity.remaining_amount,
                    payment_status = entity.payment_status,
                    due_date = entity.due_date
                )
            }
            feesAdapter.updateFees(fees)
            swipeRefreshLayout.isRefreshing = false
            
            // Load payment history for each fee
            if (fees.isNotEmpty()) {
                loadAllPaymentHistories(fees)
            }
        }
        
        // Initial refresh
        lifecycleScope.launch {
            repository.refreshFees()
        }
    }

    private fun loadFeesData() {
        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            try {
                // 1. Fetch student dashboard to get student info
                val dashboardResponse = apiService.getStudentDashboard()
                val studentProgram = if (dashboardResponse.isSuccessful && dashboardResponse.body()?.success == true) {
                    dashboardResponse.body()?.data?.student?.program ?: "N/A"
                } else "N/A"



                // 2. Fetch enrolled courses
                val coursesResponse = apiService.getStudentEnrolledCourses()
                if (coursesResponse.isSuccessful && coursesResponse.body()?.success == true) {
                    val coursesList: List<StudentCourse> = coursesResponse.body()?.data ?: emptyList()

                    if (coursesList.isNotEmpty()) {
                        val totalCredits = coursesList.sumOf { it.credit_hours }
                        val calculatedTotalAmount = totalCredits * CREDIT_COST.toDouble()

                        // 3. Update total amount in DB
                        updateTotalAmountInDB(calculatedTotalAmount)

                        // 4. Fetch existing fees
                        val feesResponse = apiService.getStudentFees()
                        val existingFees: List<StudentFeeItem> = if (feesResponse.isSuccessful && feesResponse.body()?.success == true) {
                            feesResponse.body()?.data ?: emptyList()
                        } else emptyList()

                        // 5. Map fees to Fee model
                        val mappedFees = existingFees.map { fee ->
                            Fee(
                                fee_id = fee.fee_id,
                                student_id = fee.student_id,
                                program = studentProgram,
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
                                    program = studentProgram,
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

                        // 6. Update fees RecyclerView
                        feesAdapter.updateFees(mappedFees)

                        // 7. Load all payment histories for all fees
                        loadAllPaymentHistories(mappedFees)

                    } else {
                        Toast.makeText(this@StudentFeesActivity, "No courses enrolled yet", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@StudentFeesActivity, "Failed to fetch courses", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@StudentFeesActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun updateTotalAmountInDB(totalAmount: Double): Boolean {
        val apiService = RetrofitClient.getApiService(sessionManager)
        return try {
            val request = UpdateTotalFeeRequest(
                student_id = sessionManager.getUserId(),
                total_amount = totalAmount
            )
            val response = apiService.updateStudentFeeTotal(request)
            response.isSuccessful && (response.body()?.success == true)
        } catch (e: Exception) {
            false
        }
    }

    private fun loadAllPaymentHistories(fees: List<Fee>) {
        llPaymentHistoryContainer.removeAllViews()
        val apiService = RetrofitClient.getApiService(sessionManager)

        lifecycleScope.launch {
            for (fee in fees) {
                try {
                    val response = apiService.getFeePaymentHistory(fee.fee_id)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val historyList: List<PaymentHistoryItem> = response.body()?.data ?: emptyList()

                        historyList.forEach { payment ->
                            val card = layoutInflater.inflate(R.layout.payment_history_card, llPaymentHistoryContainer, false)
                            val tvAmount = card.findViewById<TextView>(R.id.tvAmount)
                            val tvMethod = card.findViewById<TextView>(R.id.tvMethod)
                            val tvDate = card.findViewById<TextView>(R.id.tvDate)

                            tvAmount.text = "PKR ${String.format("%.2f", payment.amount_paid)}"
                            tvMethod.text = "Method: ${payment.payment_method}"
                            tvDate.text = "Date: ${formatDate(payment.created_at)}"

                            llPaymentHistoryContainer.addView(card)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load payment history for fee ${fee.fee_id}", e)
                }
            }
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

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
