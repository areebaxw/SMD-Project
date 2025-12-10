package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Schedule

class ScheduleAdapter(
    private var schedules: List<Schedule>
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvCourseCode: TextView = view.findViewById(R.id.tvCourseCode)
        val tvDayOfWeek: TextView = view.findViewById(R.id.tvDayOfWeek)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvRoom: TextView = view.findViewById(R.id.tvRoom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        
        holder.tvCourseName.text = schedule.course_name
        holder.tvCourseCode.text = schedule.course_code
        holder.tvDayOfWeek.text = schedule.day_of_week
        holder.tvTime.text = "${formatTime(schedule.start_time)} - ${formatTime(schedule.end_time)}"
        holder.tvRoom.text = "Room: ${schedule.room_number ?: "Not assigned"}"
    }

    override fun getItemCount() = schedules.size

    fun updateSchedules(newSchedules: List<Schedule>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }

    private fun formatTime(time: String): String {
        // If time is in format HH:MM:SS, convert to HH:MM
        return if (time.length > 5) {
            time.substring(0, 5)
        } else {
            time
        }
    }
}
