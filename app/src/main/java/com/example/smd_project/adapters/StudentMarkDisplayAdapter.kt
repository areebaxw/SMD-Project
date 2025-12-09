package com.example.smd_project.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.databinding.ItemStudentMarksBinding
import com.example.smd_project.models.Mark

class StudentMarkDisplayAdapter(private var marks: List<Mark>) : 
    RecyclerView.Adapter<StudentMarkDisplayAdapter.MarkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkViewHolder {
        val binding = ItemStudentMarksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MarkViewHolder(binding)
    }

    override fun getItemCount(): Int = marks.size

    override fun onBindViewHolder(holder: MarkViewHolder, position: Int) {
        holder.bind(marks[position])
    }

    fun updateMarks(newMarks: List<Mark>) {
        marks = newMarks
        notifyDataSetChanged()
    }

    inner class MarkViewHolder(private val binding: ItemStudentMarksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mark: Mark) {
            binding.apply {
                // Set title and course info
                tvTitle.text = mark.title
                tvCourseInfo.text = "${mark.course_code} - ${mark.course_name}"
                tvEvaluationType.text = mark.evaluation_type ?: "Evaluation"
                
                // Set marks - obtained marks in tvMarks, total in tvPercentage
                tvMarks.text = mark.obtained_marks.toInt().toString()
                tvPercentage.text = "/${mark.total_marks}"
                
                // Apply color based on percentage
                val color = when {
                    mark.percentage >= 80 -> Color.parseColor("#0DE322") // Green
                    mark.percentage >= 60 -> Color.parseColor("#2196F3") // Blue
                    else -> Color.parseColor("#F44336") // Red
                }
                tvMarks.setTextColor(color)
            }
        }
    }
}
