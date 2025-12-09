package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.databinding.ItemStudentEvaluationBinding
import com.example.smd_project.models.CourseEvaluation

class CourseEvaluationsAdapter(
    private val evaluations: List<CourseEvaluation>
) : RecyclerView.Adapter<CourseEvaluationsAdapter.EvaluationViewHolder>() {

    inner class EvaluationViewHolder(private val binding: ItemStudentEvaluationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(evaluation: CourseEvaluation) {
            binding.apply {
                tvTitle.text = evaluation.title
                tvCourseInfo.text = "${evaluation.course_code} - ${evaluation.course_name}"
                tvTotalMarks.text = evaluation.total_marks.toString()
                
                // Display status based on obtained marks
                if (evaluation.obtained_marks != null && evaluation.obtained_marks > 0) {
                    tvStatus.text = "Completed"
                    tvStatus.setTextColor(android.graphics.Color.GREEN)
                } else {
                    tvStatus.text = "Pending"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#8B2072"))
                }
                
                // Display due date if available
                if (!evaluation.due_date.isNullOrEmpty()) {
                    tvDueDate.text = "Due: ${evaluation.due_date}"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvaluationViewHolder {
        val binding = ItemStudentEvaluationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EvaluationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EvaluationViewHolder, position: Int) {
        holder.bind(evaluations[position])
    }

    override fun getItemCount() = evaluations.size

    fun updateEvaluations(newEvaluations: List<CourseEvaluation>) {
        (evaluations as? MutableList)?.apply {
            clear()
            addAll(newEvaluations)
            notifyDataSetChanged()
        }
    }
}
