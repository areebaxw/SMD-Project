package com.example.smd_project.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.databinding.ItemAttendanceSummaryBinding
import com.example.smd_project.models.AttendanceSummary

class AttendanceSummaryAdapter(
    private val attendance: List<AttendanceSummary>
) : RecyclerView.Adapter<AttendanceSummaryAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(private val binding: ItemAttendanceSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AttendanceSummary) {
            binding.apply {
                tvCourseName.text = item.course_name
                tvCourseCode.text = item.course_code
                
                // Display attendance breakdown
                tvPresent.text = "Present: ${item.present}"
                tvAbsent.text = "Absent: ${item.absent}"
                tvLate.text = "Late: ${item.late}"
                
                if (item.excused > 0) {
                    tvExcused.text = "Excused: ${item.excused}"
                    tvExcused.visibility = android.view.View.VISIBLE
                } else {
                    tvExcused.visibility = android.view.View.GONE
                }
                
                tvTotal.text = "Total: ${item.total}"
                
                // Display percentage with color coding
                tvPercentage.text = String.format("%.1f%%", item.percentage)
                when {
                    item.percentage >= 85 -> tvPercentage.setTextColor(Color.parseColor("#4CAF50"))
                    item.percentage >= 75 -> tvPercentage.setTextColor(Color.parseColor("#8BC34A"))
                    item.percentage >= 65 -> tvPercentage.setTextColor(Color.parseColor("#FFC107"))
                    item.percentage >= 55 -> tvPercentage.setTextColor(Color.parseColor("#FF9800"))
                    else -> tvPercentage.setTextColor(Color.parseColor("#F44336"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceSummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(attendance[position])
    }

    override fun getItemCount() = attendance.size

    fun updateAttendance(newAttendance: List<AttendanceSummary>) {
        (attendance as? MutableList)?.apply {
            clear()
            addAll(newAttendance)
            notifyDataSetChanged()
        }
    }
}
