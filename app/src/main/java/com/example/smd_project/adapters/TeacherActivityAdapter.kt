package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.TeacherActivity

class TeacherActivityAdapter(
    private var activities: List<TeacherActivity>,
    private val onActivityClick: (TeacherActivity) -> Unit = {}
) : RecyclerView.Adapter<TeacherActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvActivityTitle: TextView = view.findViewById(R.id.tvActivityTitle)
        val tvActivityDescription: TextView = view.findViewById(R.id.tvActivityDescription)
        val tvActivityTime: TextView = view.findViewById(R.id.tvActivityTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]
        
        holder.tvActivityTitle.text = activity.title
        holder.tvActivityDescription.text = activity.description
        holder.tvActivityTime.text = activity.getFormattedTime()

        holder.itemView.setOnClickListener {
            onActivityClick(activity)
        }
    }

    override fun getItemCount() = activities.size

    fun updateActivities(newList: List<TeacherActivity>) {
        activities = newList
        notifyDataSetChanged()
    }

    fun addActivity(activity: TeacherActivity) {
        val newList = listOf(activity) + activities
        updateActivities(newList)
    }
}
