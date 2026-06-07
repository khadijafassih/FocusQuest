package com.example.focusquest.data.db.dao

import androidx.room.*
import com.example.focusquest.data.db.entity.AchievementEntity

@Dao
interface AchievementDao {
    @Query("SELECT id FROM achievements WHERE username = :username")
    suspend fun getUnlockedIds(username: String): List<String>

    @Query("SELECT * FROM achievements WHERE username = :username")
    suspend fun getAll(username: String): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: AchievementEntity)
}
