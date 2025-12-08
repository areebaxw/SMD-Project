package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.EvaluationWithMarks
import com.example.smd_project.models.Student

class EvaluationMarksAdapter(
    private var evaluations: List<EvaluationWithMarks>,
    private var students: List<Student>
) : RecyclerView.Adapter<EvaluationMarksAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEvaluationNumber: TextView = itemView.findViewById(R.id.tvEvaluationNumber)
        private val tvTopicName: TextView = itemView.findViewById(R.id.tvTopicName)
        private val tvTotalMarks: TextView = itemView.findViewById(R.id.tvTotalMarks)
        private val tvMarkedCount: TextView = itemView.findViewById(R.id.tvMarkedCount)
        private val tvMarksList: TextView = itemView.findViewById(R.id.tvMarksList)
        
        fun bind(evaluation: EvaluationWithMarks) {
            // Evaluation Number
            tvEvaluationNumber.text = "Evaluation #${evaluation.evaluation_number}"
            
            // Topic Name
            tvTopicName.text = "Topic: ${evaluation.title}"
            
            // Total Marks
            tvTotalMarks.text = "Total Marks: ${evaluation.total_marks}"
            
            // Count of marked students
            val markedCount = evaluation.marks.count { (it.obtained_marks ?: 0.0) > 0 }
            tvMarkedCount.text = "Marked: $markedCount/${students.size}"
            
            // Build marks list
            val marksList = StringBuilder()
            val sortedMarks = evaluation.marks.sortedBy { it.student_id }
            for (mark in sortedMarks) {
                val student = students.find { it.student_id == mark.student_id }
                if (student != null) {
                    val obtainedMarks = mark.obtained_marks ?: 0.0
                    if (obtainedMarks > 0) {
                        marksList.append("${student.full_name}: $obtainedMarks/${evaluation.total_marks}\n")
                    }
                }
            }
            
            if (marksList.isNotEmpty()) {
                tvMarksList.text = marksList.toString().trim()
            } else {
                tvMarksList.text = "No marks entered yet"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_evaluation_marks,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(evaluations[position])
    }

    override fun getItemCount() = evaluations.size

    fun updateData(newEvaluations: List<EvaluationWithMarks>, newStudents: List<Student>) {
        evaluations = newEvaluations.sortedBy { it.evaluation_number }
        students = newStudents
        notifyDataSetChanged()
    }
}

