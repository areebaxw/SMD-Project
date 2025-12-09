package com.example.smd_project.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.databinding.DialogPayFeeBinding
import com.example.smd_project.databinding.ItemStudentFeeBinding
import com.example.smd_project.models.Fee
import com.example.smd_project.models.PayFeeRequest
import com.example.smd_project.network.RetrofitClient
import com.example.smd_project.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class StudentFeesAdapter(
    private var fees: List<Fee>,
    private val sessionManager: SessionManager
) : RecyclerView.Adapter<StudentFeesAdapter.FeeViewHolder>() {

    inner class FeeViewHolder(private val binding: ItemStudentFeeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(fee: Fee) {
            try {
                Log.d("FeeAdapter", "Binding fee: $fee")

                binding.apply {
                    // Program & Semester
                    tvProgram.text = fee.program
                    tvSemester.text = "Semester ${fee.semester} - ${fee.academic_year}"

                    // Amounts
                    val totalAmount = fee.total_amount ?: 0.0
                    val paidAmount = fee.paid_amount ?: 0.0
                    val remainingAmount = fee.remaining_amount ?: totalAmount - paidAmount

                    Log.d("FeeAdapter", "Amounts -> total: $totalAmount, paid: $paidAmount, remaining: $remainingAmount")

                    tvTotalAmount.text = "PKR ${String.format("%.2f", totalAmount)}"
                    tvPaidAmount.text = "PKR ${String.format("%.2f", paidAmount)}"
                    tvRemainingAmount.text = "PKR ${String.format("%.2f", remainingAmount)}"

                    // Due date
                    tvDueDate.text = fee.due_date?.let { formatDate(it) } ?: "Due date not set"

                    // Payment status with color
                    when (fee.payment_status) {
                        "Paid" -> {
                            tvStatus.text = "Paid"
                            tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                        }
                        "Partial" -> {
                            tvStatus.text = "Partial"
                            tvStatus.setTextColor(Color.parseColor("#FF9800"))
                        }
                        "Pending" -> {
                            tvStatus.text = "Pending"
                            tvStatus.setTextColor(Color.parseColor("#FF5722"))
                        }
                        "Overdue" -> {
                            tvStatus.text = "Overdue"
                            tvStatus.setTextColor(Color.RED)
                        }
                        else -> {
                            tvStatus.text = fee.payment_status
                            tvStatus.setTextColor(Color.GRAY)
                        }
                    }

                    // Pay Fee button click
                    btnPayFee.setOnClickListener {
                        showPayFeeDialog(fee)
                    }
                }
            } catch (e: Exception) {
                Log.e("FeeAdapter", "Error binding fee: ${fee.fee_id}", e)
            }
        }

        private fun showPayFeeDialog(fee: Fee) {
            val context = binding.root.context
            val dialogBinding = DialogPayFeeBinding.inflate(LayoutInflater.from(context))
            val dialog = AlertDialog.Builder(context)
                .setView(dialogBinding.root)
                .create()

            // Payment methods
            val methods = listOf("Cash", "Bank Transfer", "Credit Card", "E-Wallet")
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, methods)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.spinnerPaymentMethod.adapter = adapter

            // Cancel
            dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

            // Pay button
            dialogBinding.btnPay.setOnClickListener {
                val amountText = dialogBinding.etAmount.text.toString()
                Log.d("FeeAdapter", "Entered amount text: '$amountText'")
                val amount = amountText.toDoubleOrNull()
                val method = dialogBinding.spinnerPaymentMethod.selectedItem.toString()
                val remarks = dialogBinding.etRemarks.text.toString().takeIf { it.isNotBlank() }

                if (amount == null || amount <= 0) {
                    Log.e("FeeAdapter", "Invalid amount: '$amountText'")
                    Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val safeRemaining = fee.remaining_amount ?: 0.0
                Log.d("FeeAdapter", "Comparing amount $amount with remaining $safeRemaining")

                if (amount > safeRemaining) {
                    Toast.makeText(context, "Amount cannot exceed remaining fee", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                dialog.dismiss()

                // Call payStudentFee API
                (context as? AppCompatActivity)?.lifecycleScope?.launch {
                    try {
                        val apiService = RetrofitClient.getApiService(sessionManager)
                        Log.d("FeeAdapter", "Calling payStudentFee API for fee_id=${fee.fee_id}, student_id=${fee.student_id}")

                        val response = apiService.payStudentFee(
                            PayFeeRequest(
                                student_id = fee.student_id,
                                fee_id = fee.fee_id,
                                amount = amount,
                                method = method,
                                remarks = remarks
                            )
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            Log.d("FeeAdapter", "Payment successful for fee_id=${fee.fee_id}")

                            // Update local data
                            fee.paid_amount = (fee.paid_amount ?: 0.0) + amount
                            fee.remaining_amount = fee.total_amount - fee.paid_amount
                            fee.payment_status = when {
                                fee.paid_amount >= fee.total_amount -> "Paid"
                                fee.paid_amount > 0 -> "Partial"
                                else -> "Pending"
                            }

                            notifyItemChanged(adapterPosition)
                            Toast.makeText(context, "Payment successful", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("FeeAdapter", "Payment API failed: ${response.errorBody()?.string()}")
                            Toast.makeText(context, "Payment failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("FeeAdapter", "Error during API call", e)
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            dialog.show()
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                Log.e("FeeAdapter", "Failed to parse date: $dateString", e)
                dateString
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeeViewHolder {
        val binding = ItemStudentFeeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeeViewHolder, position: Int) {
        holder.bind(fees[position])
    }

    override fun getItemCount() = fees.size

    fun updateFees(newFees: List<Fee>) {
        Log.d("FeeAdapter", "Updating fees list with size=${newFees.size}")
        fees = newFees
        notifyDataSetChanged()
    }
    fun getFeeAt(position: Int): Fee = fees[position]

}
