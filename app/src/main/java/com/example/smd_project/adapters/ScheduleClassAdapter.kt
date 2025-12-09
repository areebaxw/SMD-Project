package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Schedule

class ScheduleClassAdapter(
    private val classes: List<Schedule>
) : RecyclerView.Adapter<ScheduleClassAdapter.ClassViewHolder>() {

    inner class ClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStartTime: TextView = view.findViewById(R.id.tvStartTime)
        val tvEndTime: TextView = view.findViewById(R.id.tvEndTime)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvCourseCode: TextView = view.findViewById(R.id.tvCourseCode)
        val tvRoomNumber: TextView = view.findViewById(R.id.tvRoomNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_class, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val schedule = classes[position]

        // Format time (remove seconds if present)
        holder.tvStartTime.text = formatTime(schedule.start_time)
        holder.tvEndTime.text = formatTime(schedule.end_time)
        holder.tvCourseName.text = schedule.course_name
        holder.tvCourseCode.text = schedule.course_code
        holder.tvRoomNumber.text = "Room ${schedule.room_number ?: "TBA"}"
    }

    override fun getItemCount() = classes.size

    private fun formatTime(time: String): String {
        // If time is in format HH:MM:SS, convert to HH:MM
        return if (time.length > 5) {
            time.substring(0, 5)
        } else {
            time
        }
    }
}
