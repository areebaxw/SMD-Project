package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Schedule

class DayScheduleAdapter(
    private var scheduleByDay: Map<String, List<Schedule>>
) : RecyclerView.Adapter<DayScheduleAdapter.DayViewHolder>() {

    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayName: TextView = view.findViewById(R.id.tvDayName)
        val rvClasses: RecyclerView = view.findViewById(R.id.rvClasses)
        val tvNoClasses: TextView = view.findViewById(R.id.tvNoClasses)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_schedule, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        val classes = scheduleByDay[day] ?: emptyList()

        holder.tvDayName.text = day

        if (classes.isEmpty()) {
            holder.rvClasses.visibility = View.GONE
            holder.tvNoClasses.visibility = View.VISIBLE
        } else {
            holder.rvClasses.visibility = View.VISIBLE
            holder.tvNoClasses.visibility = View.GONE

            val classAdapter = ScheduleClassAdapter(classes)
            holder.rvClasses.apply {
                layoutManager = LinearLayoutManager(holder.itemView.context)
                adapter = classAdapter
            }
        }
    }

    override fun getItemCount() = days.size

    fun updateSchedule(newSchedule: Map<String, List<Schedule>>) {
        scheduleByDay = newSchedule
        notifyDataSetChanged()
    }
}
