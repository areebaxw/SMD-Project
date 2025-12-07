package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Student
import com.squareup.picasso.Picasso

class StudentAdapter(
    private var students: List<Student>,
    private val showCheckbox: Boolean = false,
    private val onStudentChecked: ((Student, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    private val selectedStudents = mutableSetOf<Int>()

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProfilePic: ImageView = view.findViewById(R.id.ivProfilePic)
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val tvRollNo: TextView = view.findViewById(R.id.tvRollNo)
        val cbAttendance: CheckBox = view.findViewById(R.id.cbAttendance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        
        holder.tvStudentName.text = student.full_name
        holder.tvRollNo.text = student.roll_no
        
        // Load profile picture
        if (!student.profile_picture_url.isNullOrEmpty()) {
            Picasso.get()
                .load(student.profile_picture_url)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.ivProfilePic)
        }
        
        if (showCheckbox) {
            holder.cbAttendance.visibility = View.VISIBLE
            holder.cbAttendance.isChecked = selectedStudents.contains(student.student_id)
            
            holder.cbAttendance.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedStudents.add(student.student_id)
                } else {
                    selectedStudents.remove(student.student_id)
                }
                onStudentChecked?.invoke(student, isChecked)
            }
        } else {
            holder.cbAttendance.visibility = View.GONE
        }
    }

    override fun getItemCount() = students.size

    fun updateStudents(newList: List<Student>) {
        students = newList
        notifyDataSetChanged()
    }

    fun getSelectedStudents(): Set<Int> = selectedStudents

    fun clearSelection() {
        selectedStudents.clear()
        notifyDataSetChanged()
    }
}
