package com.example.smd_project.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.AttendanceSummary

class AttendanceAdapter(
    private var attendanceList: List<AttendanceSummary>,
    private val onAttendanceClick: (AttendanceSummary) -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourseCode: TextView = view.findViewById(R.id.tvCourseCode)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvAttendanceStats: TextView = view.findViewById(R.id.tvAttendanceStats)
        val tvPercentage: TextView = view.findViewById(R.id.tvPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val attendance = attendanceList[position]
        
        holder.tvCourseCode.text = "Student #${attendance.student_id}"
        holder.tvCourseName.text = attendance.full_name
        holder.tvAttendanceStats.text = 
            "Present: ${attendance.present_count} | Absent: ${attendance.absent_count} | Late: ${attendance.late_count}"
        holder.tvPercentage.text = "${attendance.attendance_percentage}%"
        
        // Color code percentage
        when {
            attendance.attendance_percentage >= 75.0 -> holder.tvPercentage.setTextColor(Color.parseColor("#4CAF50"))
            attendance.attendance_percentage >= 60.0 -> holder.tvPercentage.setTextColor(Color.parseColor("#FF9800"))
            else -> holder.tvPercentage.setTextColor(Color.parseColor("#F44336"))
        }
        
        holder.itemView.setOnClickListener {
            onAttendanceClick(attendance)
        }
    }

    override fun getItemCount() = attendanceList.size

    fun updateAttendance(newList: List<AttendanceSummary>) {
        attendanceList = newList
        notifyDataSetChanged()
    }
}
