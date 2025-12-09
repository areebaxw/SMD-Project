package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smd_project.R
import com.example.smd_project.models.StudentFinalGrade

class FinalGradesAdapter : ListAdapter<StudentFinalGrade, FinalGradesAdapter.GradeViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student_final_grade, parent, false)
        return GradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.ivProfile)
        private val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvMarks: TextView = itemView.findViewById(R.id.tvTotalMarks)
        private val tvLetter: TextView = itemView.findViewById(R.id.tvLetterGrade)
        private val tvGPA: TextView = itemView.findViewById(R.id.tvGPA)

        fun bind(student: StudentFinalGrade) {
            tvName.text = student.fullName
            tvMarks.text = "Marks: ${student.totalMarks}/100"
            tvLetter.text = "Grade: ${student.letterGrade}"
            tvGPA.text = "GPA: ${student.gpa}"

            Glide.with(itemView.context)
                .load(student.profilePic)
                .placeholder(R.drawable.profile_circle_bg)
                .error(R.drawable.profile_circle_bg)
                .circleCrop()
                .into(profileImage)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StudentFinalGrade>() {
        override fun areItemsTheSame(oldItem: StudentFinalGrade, newItem: StudentFinalGrade): Boolean =
            oldItem.studentId == newItem.studentId

        override fun areContentsTheSame(oldItem: StudentFinalGrade, newItem: StudentFinalGrade): Boolean =
            oldItem == newItem
    }
}
