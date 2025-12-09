package com.example.smd_project.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.databinding.ItemAttendanceRecordBinding
import com.example.smd_project.models.AttendanceRecord
import java.text.SimpleDateFormat
import java.util.*

class AttendanceRecordAdapter(
    private val records: MutableList<AttendanceRecord> = mutableListOf()
) : RecyclerView.Adapter<AttendanceRecordAdapter.AttendanceRecordViewHolder>() {

    inner class AttendanceRecordViewHolder(private val binding: ItemAttendanceRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: AttendanceRecord) {
            binding.apply {
                // Format date
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val date = inputFormat.parse(record.attendance_date)
                    val formattedDate = outputFormat.format(date ?: Date())
                    
                    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                    val dayOfWeek = dayFormat.format(date ?: Date())
                    
                    tvDate.text = formattedDate
                    tvDayOfWeek.text = dayOfWeek
                } catch (e: Exception) {
                    tvDate.text = record.attendance_date
                    tvDayOfWeek.text = ""
                }
                
                // Set status
                tvStatus.text = record.status
                
                // Set status icon and color
                when (record.status) {
                    "Present" -> {
                        ivStatusIcon.setImageResource(R.drawable.greentick)
                        tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                    }
                    "Absent" -> {
                        ivStatusIcon.setImageResource(R.drawable.ic_delete)
                        tvStatus.setTextColor(Color.parseColor("#F44336"))
                    }
                    "Late" -> {
                        ivStatusIcon.setImageResource(R.drawable.late)
                        tvStatus.setTextColor(Color.parseColor("#FFC107"))
                    }
                    "Excused" -> {
                        ivStatusIcon.setImageResource(R.drawable.purpletick)
                        tvStatus.setTextColor(Color.parseColor("#9C27B0"))
                    }
                    else -> {
                        ivStatusIcon.setImageResource(R.drawable.ic_launcher_foreground)
                        tvStatus.setTextColor(Color.parseColor("#666666"))
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceRecordViewHolder {
        val binding = ItemAttendanceRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttendanceRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceRecordViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount() = records.size

    fun updateRecords(newRecords: List<AttendanceRecord>) {
        android.util.Log.d("AttendanceRecordAdapter", "updateRecords called with ${newRecords.size} items")
        records.clear()
        records.addAll(newRecords)
        android.util.Log.d("AttendanceRecordAdapter", "Records list now has ${records.size} items")
        notifyDataSetChanged()
    }
}
