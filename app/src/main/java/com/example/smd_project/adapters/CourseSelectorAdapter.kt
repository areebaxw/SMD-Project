package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R

class CourseSelectorAdapter(
    private val courses: List<Pair<String, String>>, // Pair of (courseCode, courseName)
    private val onCourseSelected: (Int) -> Unit
) : RecyclerView.Adapter<CourseSelectorAdapter.CourseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_selector, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position], position)
    }

    override fun getItemCount(): Int = courses.size

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tvCourseName)
        private val tvCourseCode: TextView = itemView.findViewById(R.id.tvCourseCode)

        fun bind(course: Pair<String, String>, position: Int) {
            tvCourseCode.text = course.first
            tvCourseName.text = course.second
            
            itemView.setOnClickListener {
                onCourseSelected(position)
            }
        }
    }
}
