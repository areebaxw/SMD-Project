package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.smd_project.R

import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.models.TranscriptCourse

class TranscriptCourseAdapter(private var courses: List<TranscriptCourse>) :
    RecyclerView.Adapter<TranscriptCourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCourseName: TextView = itemView.findViewById(R.id.tv_course_name)
        val tvCourseCode: TextView = itemView.findViewById(R.id.tv_course_code)
        val tvCredits: TextView = itemView.findViewById(R.id.tv_course_credits)
        val tvMarks: TextView = itemView.findViewById(R.id.tv_course_marks)
        val tvGrade: TextView = itemView.findViewById(R.id.tv_course_grade)
        val tvGradePoints: TextView = itemView.findViewById(R.id.tv_course_gp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transcript_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun getItemCount(): Int = courses.size

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.tvCourseName.text = course.course_name
        holder.tvCourseCode.text = course.course_code
        holder.tvCredits.text = "Credits: ${course.credit_hours}"
        holder.tvMarks.text = "Marks: ${course.marks}/100"
        holder.tvGrade.text = course.grade
        holder.tvGradePoints.text = "${course.grade_points} GP"
    }

    fun updateCourses(newCourses: List<TranscriptCourse>) {
        courses = newCourses
        notifyDataSetChanged()
    }
}
