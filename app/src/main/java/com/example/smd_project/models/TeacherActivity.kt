package com.example.smd_project.models

import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TeacherActivity(
    val activity_id: String = System.currentTimeMillis().toString(),
    val activity_type: String, // "announcement", "attendance", "marks", "grades", "schedule"
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val icon: Int = 0, // Resource ID for icon
    val relatedId: Int? = null, // ID of related resource (course_id, announcement_id, etc)
    val relatedName: String? = null // Name of related resource (course_name, etc)
) : Serializable {
    fun getFormattedTime(): String {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - timestamp

        return when {
            timeDiff < 60000 -> "Just now"
            timeDiff < 3600000 -> "${timeDiff / 60000}m ago"
            timeDiff < 86400000 -> "${timeDiff / 3600000}h ago"
            timeDiff < 604800000 -> "${timeDiff / 86400000}d ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                val date = java.util.Date(timestamp)
                formatter.format(date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
            }
        }
    }

    companion object {
        const val TYPE_ANNOUNCEMENT = "announcement"
        const val TYPE_ATTENDANCE = "attendance"
        const val TYPE_MARKS = "marks"
        const val TYPE_GRADES = "grades"
        const val TYPE_SCHEDULE = "schedule"
    }
}
