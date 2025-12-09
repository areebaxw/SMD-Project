package com.example.smd_project.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Student

class StudentMarkEntryAdapter(
    private var students: List<Student>,
    private var marksMap: MutableMap<Int, Double>,
    private var totalMarks: Int = 50,
    private val onMarksChange: (studentId: Int, marks: Double) -> Unit,
    private val onMarksEntered: (studentId: Int, hasValue: Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<StudentMarkEntryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvRollNo: TextView = itemView.findViewById(R.id.tvRollNo)
        private val tvAvatarInitial: TextView = itemView.findViewById(R.id.tvAvatarInitial)
        private val etMarks: EditText = itemView.findViewById(R.id.etMarks)
        private val tvMaxMarks: TextView = itemView.findViewById(R.id.tvMaxMarks)

        fun bind(student: Student) {
            tvStudentName.text = student.full_name
            tvRollNo.text = student.roll_no
            
            // Set avatar initial from name
            val initial = student.full_name.take(1).uppercase()
            tvAvatarInitial.text = initial

            // Set max marks label dynamically
            tvMaxMarks.text = "/ $totalMarks"

            // Remove old listeners before setting up new ones
            etMarks.removeTextChangedListener(null) // Remove all listeners
            etMarks.setOnFocusChangeListener(null)

            // Set existing marks if any (including 0)
            val currentMarks = marksMap[student.student_id]
            if (currentMarks != null) {
                if (currentMarks > 0) {
                    etMarks.setText(currentMarks.toString())
                } else {
                    etMarks.setText("") // Show empty for 0 marks
                }
            } else {
                etMarks.setText("")
            }

            // Listen to real-time text changes
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val hasValue = s?.toString()?.trim()?.isNotEmpty() ?: false
                    val marks = if (hasValue) {
                        s.toString().toDoubleOrNull() ?: 0.0
                    } else {
                        0.0
                    }
                    
                    // Update map only if marks are valid (0 or less than total)
                    if (marks >= 0 && marks <= totalMarks) {
                        marksMap[student.student_id] = marks
                        onMarksChange(student.student_id, marks)
                        // Call onMarksEntered if there's actually a value (even if it's being edited)
                        onMarksEntered(student.student_id, hasValue)
                    }
                }
                
                override fun afterTextChanged(s: Editable?) {}
            }
            etMarks.addTextChangedListener(textWatcher)
            
            // Also listen to focus change for final confirmation
            etMarks.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val marks = etMarks.text.toString().toDoubleOrNull() ?: 0.0
                    // Cap marks to total marks if user entered more
                    val finalMarks = if (marks > totalMarks) {
                        totalMarks.toDouble()
                    } else {
                        marks
                    }
                    etMarks.setText(if (finalMarks > 0) finalMarks.toString() else "")
                    marksMap[student.student_id] = finalMarks
                    onMarksChange(student.student_id, finalMarks)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_student_mark_entry,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(students[position])
    }

    override fun getItemCount() = students.size

    fun updateStudents(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }

    fun updateMarksMap(newMarksMap: Map<Int, Double>) {
        marksMap.clear()
        marksMap.putAll(newMarksMap)
        notifyDataSetChanged()
    }

    fun updateTotalMarks(newTotalMarks: Int) {
        totalMarks = newTotalMarks
        notifyDataSetChanged()
    }
}
