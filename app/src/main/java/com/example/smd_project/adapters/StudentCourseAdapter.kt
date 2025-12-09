package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.databinding.ItemStudentCourseBinding
import com.example.smd_project.models.StudentCourse

class StudentCourseAdapter(
    private val courses: List<StudentCourse>,
    private val onCourseClick: (StudentCourse) -> Unit
) : RecyclerView.Adapter<StudentCourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(private val binding: ItemStudentCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(course: StudentCourse) {
            binding.apply {
                tvCourseName.text = course.course_name
                tvCourseCode.text = course.course_code
                tvTeacherName.text = "Prof. ${course.teacher_name}"
                tvCredits.text = "${course.credit_hours} Credits"

                // Display grade if available
                if (!course.grade.isNullOrEmpty()) {
                    tvGrade.text = course.grade
                    tvGrade.visibility = android.view.View.VISIBLE
                } else {
                    tvGrade.visibility = android.view.View.GONE
                }

                // Display GPA if available
                if (course.gpa != null && course.gpa > 0) {
                    tvGPA.text = String.format("%.2f", course.gpa)
                    tvGPA.visibility = android.view.View.VISIBLE
                } else {
                    tvGPA.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onCourseClick(course)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemStudentCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount() = courses.size
}
