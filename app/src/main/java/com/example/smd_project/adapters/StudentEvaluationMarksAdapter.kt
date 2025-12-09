package com.example.smd_project.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.EvaluationWithMarks

class StudentEvaluationMarksAdapter(
    private var evaluations: List<EvaluationWithMarks>,
    private val studentId: Int
) : RecyclerView.Adapter<StudentEvaluationMarksAdapter.EvaluationViewHolder>() {

    inner class EvaluationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEvaluationTitle: TextView = itemView.findViewById(R.id.tvEvaluationTitle)
        val tvWeightage: TextView = itemView.findViewById(R.id.tvWeightage)
        val tvAverage: TextView = itemView.findViewById(R.id.tvAverage)
        val tvHighest: TextView = itemView.findViewById(R.id.tvHighest)
        val tvLowest: TextView = itemView.findViewById(R.id.tvLowest)
        val tvObtainedMarks: TextView = itemView.findViewById(R.id.tvObtainedMarks)
        val tvTotalMarks: TextView = itemView.findViewById(R.id.tvTotalMarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvaluationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evaluation_mark, parent, false)
        return EvaluationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EvaluationViewHolder, position: Int) {
        val evaluation = evaluations[position]
        
        // Find student's marks in this evaluation
        val studentMark = evaluation.marks.find { it.student_id == studentId }
        val obtainedMarks = studentMark?.obtained_marks ?: 0.0
        
        // Set evaluation title
        holder.tvEvaluationTitle.text = evaluation.title
        
        // Set weightage
        holder.tvWeightage.text = "Weightage: ${evaluation.weightage ?: "-"}"
        
        // Set stats
        val stats = evaluation.stats
        holder.tvAverage.text = "Average: ${stats?.average?.let { String.format("%.1f", it) } ?: "-"}"
        holder.tvHighest.text = "Highest: ${stats?.highest?.let { String.format("%.1f", it) } ?: "-"}"
        holder.tvLowest.text = "Lowest: ${stats?.lowest?.let { String.format("%.1f", it) } ?: "-"}"
        
        // Set obtained marks
        holder.tvObtainedMarks.text = String.format("%.0f", obtainedMarks)
        holder.tvTotalMarks.text = "/${evaluation.total_marks}"
        
        // Set color based on performance
        val percentage = (obtainedMarks / evaluation.total_marks) * 100
        val color = when {
            percentage >= 80 -> Color.parseColor("#0DE322") // Green
            percentage >= 60 -> Color.parseColor("#2196F3") // Blue
            else -> Color.parseColor("#F44336") // Red
        }
        holder.tvObtainedMarks.setTextColor(color)
    }

    override fun getItemCount(): Int = evaluations.size

    fun updateData(newEvaluations: List<EvaluationWithMarks>) {
        evaluations = newEvaluations
        notifyDataSetChanged()
    }
}
