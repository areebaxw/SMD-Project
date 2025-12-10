package com.example.smd_project.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Notification
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class StudentNotificationAdapter(
    private val notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit,
    private val onDeleteClick: ((Notification) -> Unit)? = null
) : RecyclerView.Adapter<StudentNotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.notificationCard)
        val tvTitle: TextView = view.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotificationTime)
        val ivIcon: ImageView = view.findViewById(R.id.ivNotificationIcon)
        val unreadDot: View = view.findViewById(R.id.unreadDot)
        val ivDelete: ImageView? = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        val context = holder.itemView.context

        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message
        holder.tvTime.text = getTimeAgo(notification.created_at)

        // Set icon based on notification type
        val iconRes = when (notification.notification_type.lowercase()) {
            "announcement" -> R.drawable.ic_announcement
            "marks", "grade" -> R.drawable.ic_grade
            "attendance" -> R.drawable.ic_attendance
            "evaluation" -> R.drawable.evaluations
            else -> R.drawable.ic_notification
        }
        holder.ivIcon.setImageResource(iconRes)

        // Style based on read status
        if (notification.is_read == 0) {
            holder.unreadDot.visibility = View.VISIBLE
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.white))
            holder.tvTitle.setTypeface(null, Typeface.BOLD)
            holder.tvMessage.setTypeface(null, Typeface.BOLD)
            holder.cardView.cardElevation = 4f
        } else {
            holder.unreadDot.visibility = View.GONE
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.gray_light))
            holder.tvTitle.setTypeface(null, Typeface.NORMAL)
            holder.tvMessage.setTypeface(null, Typeface.NORMAL)
            holder.cardView.cardElevation = 1f
        }

        // Click listeners
        holder.cardView.setOnClickListener {
            onNotificationClick(notification)
        }

        holder.ivDelete?.setOnClickListener {
            onDeleteClick?.invoke(notification)
        }
    }

    override fun getItemCount() = notifications.size

    private fun getTimeAgo(timestamp: String): String {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = format.parse(timestamp)
            val now = System.currentTimeMillis()
            val diff = now - (date?.time ?: now)

            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "$minutes min ago"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "$hours hour${if (hours > 1) "s" else ""} ago"
                }
                diff < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    "$days day${if (days > 1) "s" else ""} ago"
                }
                else -> {
                    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    displayFormat.format(date)
                }
            }
        } catch (e: Exception) {
            return timestamp
        }
    }
}
