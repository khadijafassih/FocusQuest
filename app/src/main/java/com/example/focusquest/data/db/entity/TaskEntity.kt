package com.example.focusquest.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val title: String,
    val description: String = "",
    val category: String,
    val priority: Int = 1,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val xpReward: Int = 20
) {
    companion object {
        const val PRIORITY_LOW = 0
        const val PRIORITY_MEDIUM = 1
        const val PRIORITY_HIGH = 2

        fun xpForCategory(category: String, priority: Int): Int {
            val base = when (category) {
                "Study" -> 25
                "Work" -> 20
                "Health" -> 20
                else -> 15
            }
            return when (priority) {
                PRIORITY_HIGH -> (base * 1.5).toInt()
                PRIORITY_LOW -> (base * 0.7).toInt()
                else -> base
            }
        }
    }
}
