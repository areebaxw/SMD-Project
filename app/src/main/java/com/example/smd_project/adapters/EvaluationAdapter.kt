package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Evaluation
import java.text.SimpleDateFormat
import java.util.*

class EvaluationAdapter(
    private var evaluations: List<Evaluation>,
    private val onEvaluationClick: (Evaluation) -> Unit
) : RecyclerView.Adapter<EvaluationAdapter.EvaluationViewHolder>() {

    class EvaluationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvEvaluationTitle)
        val tvType: TextView = view.findViewById(R.id.tvEvaluationType)
        val tvTotalMarks: TextView = view.findViewById(R.id.tvTotalMarks)
        val tvDueDate: TextView = view.findViewById(R.id.tvDueDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvaluationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evaluation, parent, false)
        return EvaluationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EvaluationViewHolder, position: Int) {
        val evaluation = evaluations[position]
        
        holder.tvTitle.text = evaluation.title
        holder.tvType.text = evaluation.evaluation_type
        holder.tvTotalMarks.text = "Total Marks: ${evaluation.total_marks}"
        
        if (evaluation.due_date != null) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(evaluation.due_date)
                holder.tvDueDate.text = "Due: ${date?.let { outputFormat.format(it) }}"
            } catch (e: Exception) {
                holder.tvDueDate.text = "Due: ${evaluation.due_date}"
            }
        } else {
            holder.tvDueDate.text = "No due date"
        }
        
        holder.itemView.setOnClickListener {
            onEvaluationClick(evaluation)
        }
    }

    override fun getItemCount() = evaluations.size

    fun updateEvaluations(newEvaluations: List<Evaluation>) {
        evaluations = newEvaluations
        notifyDataSetChanged()
    }
}
