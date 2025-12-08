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
import com.example.smd_project.models.StudentMark
import com.example.smd_project.models.MarksRecordItem

class StudentMarkAdapter(
    private var students: List<StudentMark>,
    private val totalMarks: Int
) : RecyclerView.Adapter<StudentMarkAdapter.StudentMarkViewHolder>() {

    private val marksMap = mutableMapOf<Int, Double>()

    class StudentMarkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRollNo: TextView = view.findViewById(R.id.tvRollNo)
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val etMarks: EditText = view.findViewById(R.id.etMarks)
        val tvMaxMarks: TextView = view.findViewById(R.id.tvMaxMarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentMarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_mark, parent, false)
        return StudentMarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentMarkViewHolder, position: Int) {
        val student = students[position]
        
        holder.tvRollNo.text = student.roll_no
        holder.tvStudentName.text = student.full_name
        holder.tvMaxMarks.text = "/ $totalMarks"
        
        // Restore previous marks if exists
        val previousMarks = marksMap[student.student_id]
        if (previousMarks != null) {
            holder.etMarks.setText(previousMarks.toString())
        }
        
        // Add text change listener
        holder.etMarks.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val marks = s.toString().toDoubleOrNull()
                if (marks != null) {
                    marksMap[student.student_id] = marks
                } else if (s.isNullOrEmpty()) {
                    marksMap.remove(student.student_id)
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun getItemCount() = students.size

    fun updateStudents(newStudents: List<StudentMark>) {
        students = newStudents
        marksMap.clear()
        notifyDataSetChanged()
    }

    fun getMarksData(): List<MarksRecordItem> {
        return marksMap.map { (studentId, marks) ->
            MarksRecordItem(
                student_id = studentId,
                obtained_marks = marks
            )
        }
    }
}
