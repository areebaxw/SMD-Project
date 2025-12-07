package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Announcement
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementAdapter(
    private var announcements: List<Announcement>
) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    class AnnouncementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAnnouncementTitle: TextView = view.findViewById(R.id.tvAnnouncementTitle)
        val tvAnnouncementType: TextView = view.findViewById(R.id.tvAnnouncementType)
        val tvAnnouncementContent: TextView = view.findViewById(R.id.tvAnnouncementContent)
        val tvTeacherName: TextView = view.findViewById(R.id.tvTeacherName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]
        
        holder.tvAnnouncementTitle.text = announcement.title
        holder.tvAnnouncementType.text = announcement.announcement_type
        holder.tvAnnouncementContent.text = announcement.content
        holder.tvTeacherName.text = announcement.teacher_name
        
        // Format date
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(announcement.created_at)
            holder.tvDate.text = date?.let { outputFormat.format(it) } ?: announcement.created_at
        } catch (e: Exception) {
            holder.tvDate.text = announcement.created_at
        }
        
        if (announcement.course_name != null) {
            holder.tvCourseName.text = "${announcement.course_code} - ${announcement.course_name}"
            holder.tvCourseName.visibility = View.VISIBLE
        } else {
            holder.tvCourseName.visibility = View.GONE
        }
    }

    override fun getItemCount() = announcements.size

    fun updateAnnouncements(newList: List<Announcement>) {
        announcements = newList
        notifyDataSetChanged()
    }
}
