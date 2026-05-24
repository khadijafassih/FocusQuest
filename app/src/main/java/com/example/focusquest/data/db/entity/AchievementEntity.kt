package com.example.focusquest.data.db.entity

import androidx.room.Entity

@Entity(tableName = "achievements", primaryKeys = ["id", "username"])
data class AchievementEntity(
    val id: String,
    val username: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
