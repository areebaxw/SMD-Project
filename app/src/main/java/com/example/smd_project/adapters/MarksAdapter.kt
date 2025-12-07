package com.example.smd_project.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Mark

class MarksAdapter(
    private var marksList: List<Mark>
) : RecyclerView.Adapter<MarksAdapter.MarksViewHolder>() {

    class MarksViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEvaluationType: TextView = view.findViewById(R.id.tvEvaluationType)
        val tvCourseCode: TextView = view.findViewById(R.id.tvCourseCode)
        val tvObtainedMarks: TextView = view.findViewById(R.id.tvObtainedMarks)
        val tvTotalMarks: TextView = view.findViewById(R.id.tvTotalMarks)
        val tvPercentage: TextView = view.findViewById(R.id.tvPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marks, parent, false)
        return MarksViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarksViewHolder, position: Int) {
        val mark = marksList[position]
        
        holder.tvEvaluationType.text = "${mark.evaluation_type} ${mark.evaluation_number}"
        holder.tvCourseCode.text = "${mark.course_code} - ${mark.course_name}"
        holder.tvObtainedMarks.text = mark.obtained_marks.toString()
        holder.tvTotalMarks.text = mark.total_marks.toString()
        holder.tvPercentage.text = "${mark.percentage}%"
        
        // Color code percentage
        when {
            mark.percentage >= 80.0 -> holder.tvPercentage.setTextColor(Color.parseColor("#4CAF50"))
            mark.percentage >= 60.0 -> holder.tvPercentage.setTextColor(Color.parseColor("#FF9800"))
            else -> holder.tvPercentage.setTextColor(Color.parseColor("#F44336"))
        }
    }

    override fun getItemCount() = marksList.size

    fun updateMarks(newList: List<Mark>) {
        marksList = newList
        notifyDataSetChanged()
    }
}
