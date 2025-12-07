package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Course

class CourseAdapter(
    private var courses: List<Course>,
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourseCode: TextView = view.findViewById(R.id.tvCourseCode)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvCourseDetails: TextView = view.findViewById(R.id.tvCourseDetails)
        val tvSchedule: TextView = view.findViewById(R.id.tvSchedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        
        holder.tvCourseCode.text = course.course_code
        holder.tvCourseName.text = course.course_name
        
        val details = "Credits: ${course.credits}" + 
            if (course.instructors != null) " | Instructor: ${course.instructors}" else ""
        holder.tvCourseDetails.text = details
        
        holder.tvSchedule.text = course.schedule ?: "Schedule not available"
        
        holder.itemView.setOnClickListener {
            onCourseClick(course)
        }
    }

    override fun getItemCount() = courses.size

    fun updateCourses(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }
}
