package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.example.smd_project.R
import com.example.smd_project.models.Student

class MarksStudentAdapter(
    private var students: List<Student>,
    private val marksMap: MutableMap<Int, Double>,
    private val onMarksChange: (studentId: Int, marks: Double) -> Unit,
    private var maxMarks: Int = 50
) : RecyclerView.Adapter<MarksStudentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        private val studentName: TextView = itemView.findViewById(R.id.studentName)
        private val rollNumber: TextView = itemView.findViewById(R.id.rollNumber)
        private val marksInput: EditText = itemView.findViewById(R.id.marksInput)
        private val marksLabel: TextView = itemView.findViewById(R.id.marksLabel)

        fun bind(student: Student) {
            studentName.text = student.full_name
            rollNumber.text = "Roll Number: ${student.roll_no}"
            
            // Show marks label with max marks
            marksLabel.text = "Marks (/$maxMarks)"
            
            // Load profile picture
            if (!student.profile_picture_url.isNullOrEmpty()) {
                Picasso.get()
                    .load(student.profile_picture_url)
                    .into(profileImage)
            } else {
                // Set default background color for avatar
                profileImage.setImageDrawable(
                    android.graphics.drawable.ColorDrawable(
                        android.graphics.Color.parseColor("#8B2072")
                    )
                )
            }

            // Set current marks if any
            val currentMarks = marksMap[student.student_id]
            if (currentMarks != null && currentMarks > 0.0) {
                marksInput.setText(currentMarks.toInt().toString())
            }

            // Listen to text changes in real-time
            marksInput.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    val marks = s.toString().toDoubleOrNull() ?: 0.0
                    marksMap[student.student_id] = marks
                    onMarksChange(student.student_id, marks)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marks_student, parent, false)
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

    fun updateMarks(marks: MutableMap<Int, Double>) {
        marksMap.clear()
        marksMap.putAll(marks)
        notifyDataSetChanged()
    }

    fun updateMaxMarks(max: Int) {
        maxMarks = max
        notifyDataSetChanged()
    }
}
