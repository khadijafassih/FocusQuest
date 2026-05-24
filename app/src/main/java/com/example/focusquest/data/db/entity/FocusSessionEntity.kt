package com.example.focusquest.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationMinutes: Int,
    val sessionType: String,
    val completedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_FOCUS = "FOCUS"
        const val TYPE_SHORT_BREAK = "SHORT_BREAK"
        const val TYPE_LONG_BREAK = "LONG_BREAK"
    }
}
