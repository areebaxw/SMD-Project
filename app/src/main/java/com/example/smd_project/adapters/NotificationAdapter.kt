package com.example.smd_project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smd_project.R
import com.example.smd_project.models.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onNotificationClick: ((Notification) -> Unit)? = null
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNotificationTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
        val tvType: TextView = view.findViewById(R.id.tvNotificationType)
        val tvDate: TextView = view.findViewById(R.id.tvNotificationDate)
        val tvUnreadIndicator: View = view.findViewById(R.id.unreadIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        
        holder.tvTitle.text = notification.title
        holder.tvMessage.text = notification.message
        holder.tvType.text = notification.notification_type
        
        // Format date
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(notification.created_at)
            holder.tvDate.text = date?.let { outputFormat.format(it) } ?: notification.created_at
        } catch (e: Exception) {
            holder.tvDate.text = notification.created_at
        }
        
        // Show unread indicator
        holder.tvUnreadIndicator.visibility = if (!notification.is_read) View.VISIBLE else View.GONE
        
        // Set background color based on read status
        holder.itemView.alpha = if (notification.is_read) 0.6f else 1.0f
        
        holder.itemView.setOnClickListener {
            onNotificationClick?.invoke(notification)
        }
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
