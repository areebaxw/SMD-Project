package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.EvaluationWithMarks
import com.example.smd_project.models.Student

class EvaluationWithStudentsAdapter(
    private var evaluations: List<EvaluationWithMarks>,
    private var students: List<Student>,
    private var marksMap: MutableMap<Int, Double>,
    private val onMarksChange: (studentId: Int, marks: Double) -> Unit,
    private val onMarksEntered: (studentId: Int, hasValue: Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<EvaluationWithStudentsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvEvalNumber: TextView = itemView.findViewById(R.id.tvEvalNumber)
        private val tvEvalTopic: TextView = itemView.findViewById(R.id.tvEvalTopic)
        private val rvStudents: RecyclerView = itemView.findViewById(R.id.rvStudentsInEval)

        fun bind(evaluation: EvaluationWithMarks) {
            tvEvalNumber.text = "Evaluation #${evaluation.evaluation_number}"
            tvEvalTopic.text = "Topic: ${evaluation.title}"

            // Setup nested RecyclerView for students
            val studentMarkAdapter = StudentMarkEntryAdapter(
                students = students,
                marksMap = marksMap,
                totalMarks = evaluation.total_marks,
                onMarksChange = { studentId, marks ->
                    onMarksChange(studentId, marks)
                },
                onMarksEntered = { studentId, hasValue ->
                    onMarksEntered(studentId, hasValue)
                }
            )

            rvStudents.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                adapter = studentMarkAdapter
                isNestedScrollingEnabled = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_evaluation_with_students,
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

    fun updateMarksMap(newMarksMap: Map<Int, Double>) {
        marksMap.clear()
        marksMap.putAll(newMarksMap)
        notifyDataSetChanged()
    }
}
