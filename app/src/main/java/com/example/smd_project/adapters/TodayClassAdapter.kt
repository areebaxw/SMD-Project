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
        val tvCourseCode: TextView = view.findViewById(R.id.tvCourseCode)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvRoomNumber: TextView = view.findViewById(R.id.tvRoomNumber)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_class, parent, false)
        return TodayClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodayClassViewHolder, position: Int) {
        val todayClass = classes[position]
        
        holder.tvCourseCode.text = todayClass.course_code
        holder.tvCourseName.text = todayClass.course_name
        holder.tvRoomNumber.text = "Room: ${todayClass.room_number ?: "TBA"}"
        holder.tvTime.text = "${todayClass.start_time} - ${todayClass.end_time}"
    }

    override fun getItemCount() = classes.size

    fun updateClasses(newList: List<TodayClass>) {
        classes = newList
        notifyDataSetChanged()
    }
}
