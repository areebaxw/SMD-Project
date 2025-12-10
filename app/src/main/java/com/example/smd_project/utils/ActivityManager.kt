package com.example.smd_project.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.smd_project.models.TeacherActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ActivityManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("teacher_activities", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val activitiesKey = "activities_list"

    fun addActivity(activity: TeacherActivity) {
        try {
            val activities = getAllActivities().toMutableList()
            activities.add(0, activity) // Add to beginning (most recent first)

            // Keep only last 50 activities
            val limitedActivities = activities.take(50)

            val json = gson.toJson(limitedActivities)
            sharedPreferences.edit().putString(activitiesKey, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllActivities(): List<TeacherActivity> {
        return try {
            val json = sharedPreferences.getString(activitiesKey, "[]") ?: "[]"
            val type = object : TypeToken<List<TeacherActivity>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getRecentActivities(limit: Int = 10): List<TeacherActivity> {
        return getAllActivities().take(limit)
    }

    fun clearAllActivities() {
        sharedPreferences.edit().remove(activitiesKey).apply()
    }

    fun removeActivity(activityId: String) {
        try {
            val activities = getAllActivities().toMutableList()
            activities.removeAll { it.activity_id == activityId }
            val json = gson.toJson(activities)
            sharedPreferences.edit().putString(activitiesKey, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActivitiesByType(type: String): List<TeacherActivity> {
        return getAllActivities().filter { it.activity_type == type }
    }
}
