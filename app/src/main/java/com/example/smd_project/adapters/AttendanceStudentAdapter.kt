package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Student
import com.squareup.picasso.Picasso

class AttendanceStudentAdapter(
    private var students: List<Student>,
    private val onAttendanceChanged: (studentId: Int, status: String) -> Unit
) : RecyclerView.Adapter<AttendanceStudentAdapter.AttendanceViewHolder>() {

    private val attendanceStatus = mutableMapOf<Int, String>() // student_id to status

    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProfilePic: ImageView = view.findViewById(R.id.ivProfilePic)
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val tvRollNo: TextView = view.findViewById(R.id.tvRollNo)
        val btnPresent: TextView = view.findViewById(R.id.btnPresent)
        val btnAbsent: TextView = view.findViewById(R.id.btnAbsent)
        val btnLate: TextView = view.findViewById(R.id.btnLate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_student, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val student = students[position]
        val context = holder.itemView.context
        
        holder.tvStudentName.text = student.full_name
        holder.tvRollNo.text = student.roll_no
        
        // Load profile picture
        if (!student.profile_picture_url.isNullOrEmpty()) {
            Picasso.get()
                .load(student.profile_picture_url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.ivProfilePic)
        }
        
        // Get current status or default to Absent
        val currentStatus = attendanceStatus[student.student_id] ?: "Absent"
        
        // Update button states
        updateButtonStates(holder, currentStatus)
        
        // Set click listeners
        holder.btnPresent.setOnClickListener {
            attendanceStatus[student.student_id] = "Present"
            updateButtonStates(holder, "Present")
            onAttendanceChanged(student.student_id, "Present")
        }
        
        holder.btnAbsent.setOnClickListener {
            attendanceStatus[student.student_id] = "Absent"
            updateButtonStates(holder, "Absent")
            onAttendanceChanged(student.student_id, "Absent")
        }
        
        holder.btnLate.setOnClickListener {
            attendanceStatus[student.student_id] = "Late"
            updateButtonStates(holder, "Late")
            onAttendanceChanged(student.student_id, "Late")
        }
    }

    private fun updateButtonStates(holder: AttendanceViewHolder, status: String) {
        val context = holder.itemView.context
        val greenColor = context.getColor(android.R.color.holo_green_light) // #4CAF50 equivalent
        val redColor = context.getColor(android.R.color.holo_red_light) // #F44336 equivalent
        val blueColor = context.getColor(android.R.color.holo_blue_light) // #2196F3 equivalent
        val grayColor = context.getColor(android.R.color.darker_gray) // Unselected color
        
        // Reset all buttons
        holder.btnPresent.setTextColor(grayColor)
        holder.btnAbsent.setTextColor(grayColor)
        holder.btnLate.setTextColor(grayColor)
        
        // Highlight selected button
        when (status) {
            "Present" -> holder.btnPresent.setTextColor(greenColor)
            "Absent" -> holder.btnAbsent.setTextColor(redColor)
            "Late" -> holder.btnLate.setTextColor(blueColor)
        }
    }

    override fun getItemCount() = students.size

    fun updateStudents(newList: List<Student>) {
        students = newList
        attendanceStatus.clear()
        // Initialize all with Absent status
        students.forEach { student ->
            attendanceStatus[student.student_id] = "Absent"
        }
        notifyDataSetChanged()
    }

    fun updateAttendanceStatus(statusMap: Map<Int, String>) {
        attendanceStatus.clear()
        attendanceStatus.putAll(statusMap)
        notifyDataSetChanged()
    }
}
