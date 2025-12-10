package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.TodayClass

class TodayClassAdapter(
    private var classes: List<TodayClass>
) : RecyclerView.Adapter<TodayClassAdapter.TodayClassViewHolder>() {

    class TodayClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClassTime: TextView = view.findViewById(R.id.tvClassTime)
        val tvClassEndTime: TextView = view.findViewById(R.id.tvClassEndTime)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvClassDetails: TextView = view.findViewById(R.id.tvClassDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_class, parent, false)
        return TodayClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodayClassViewHolder, position: Int) {
        val todayClass = classes[position]
        
        holder.tvClassTime.text = formatTime(todayClass.start_time)
        holder.tvClassEndTime.text = formatTime(todayClass.end_time)
        holder.tvCourseName.text = todayClass.course_name
        holder.tvClassDetails.text = "Room : ${todayClass.room_number ?: "TBA"}"
    }

    override fun getItemCount() = classes.size

    fun updateClasses(newList: List<TodayClass>) {
        classes = newList
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
