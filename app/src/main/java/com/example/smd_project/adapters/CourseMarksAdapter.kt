package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.databinding.ItemStudentMarksBinding
import com.example.smd_project.models.CourseMarksDetail

class CourseMarksAdapter(
    private val marks: List<CourseMarksDetail>
) : RecyclerView.Adapter<CourseMarksAdapter.MarksViewHolder>() {

    inner class MarksViewHolder(private val binding: ItemStudentMarksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mark: CourseMarksDetail) {
            binding.apply {
                tvTitle.text = mark.title
                tvCourseInfo.text = "${mark.course_code} - ${mark.course_name}"
                tvEvaluationType.text = mark.type_name
                tvMarks.text = "${mark.obtained_marks.toInt()}/${mark.total_marks}"
                tvPercentage.text = "${mark.percentage}%"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksViewHolder {
        val binding = ItemStudentMarksBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MarksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarksViewHolder, position: Int) {
        holder.bind(marks[position])
    }

    override fun getItemCount() = marks.size

    fun updateMarks(newMarks: List<CourseMarksDetail>) {
        (marks as? MutableList)?.apply {
            clear()
            addAll(newMarks)
            notifyDataSetChanged()
        }
    }
}
