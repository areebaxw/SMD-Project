package com.example.smd_project.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.databinding.ItemStudentFeeBinding
import com.example.smd_project.models.Fee

class StudentFeesAdapter(
    private var fees: List<Fee>
) : RecyclerView.Adapter<StudentFeesAdapter.FeeViewHolder>() {

    inner class FeeViewHolder(private val binding: ItemStudentFeeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fee: Fee) {
            binding.apply {
                tvProgram.text = fee.program
                tvSemester.text = "Semester ${fee.semester} - ${fee.academic_year}"
                tvTotalAmount.text = "PKR ${String.format("%.2f", fee.total_amount)}"
                tvPaidAmount.text = "PKR ${String.format("%.2f", fee.paid_amount)}"
                tvRemainingAmount.text = "PKR ${String.format("%.2f", fee.remaining_amount)}"
                
                // Format due date if available
                if (!fee.due_date.isNullOrEmpty()) {
                    tvDueDate.text = fee.due_date
                }
                
                // Set status with appropriate color
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
        fees = newFees
        notifyDataSetChanged()
    }
}
