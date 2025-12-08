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
        
        holder.tvClassTime.text = "${todayClass.start_time}\n   to\n${todayClass.end_time}"
        holder.tvCourseName.text = todayClass.course_name
        val studentCount = todayClass.student_count ?: 0
        holder.tvClassDetails.text = "Room : ${todayClass.room_number ?: "TBA"} . $studentCount Students"
    }

    override fun getItemCount() = classes.size

    fun updateClasses(newList: List<TodayClass>) {
        classes = newList
        notifyDataSetChanged()
    }
}
