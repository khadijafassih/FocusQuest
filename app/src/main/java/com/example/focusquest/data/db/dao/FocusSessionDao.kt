package com.example.focusquest.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.focusquest.data.db.entity.FocusSessionEntity

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions WHERE username = :username ORDER BY completedAt DESC LIMIT 30")
    fun getRecentSessions(username: String): LiveData<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE username = :username AND sessionType = 'FOCUS' ORDER BY completedAt DESC")
    suspend fun getAllFocusSessions(username: String): List<FocusSessionEntity>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE username = :username AND sessionType = 'FOCUS' AND completedAt >= :since")
    suspend fun countFocusSessionsSince(username: String, since: Long): Int

    @Query("SELECT IFNULL(SUM(durationMinutes), 0) FROM focus_sessions WHERE username = :username AND sessionType = 'FOCUS' AND completedAt >= :since")
    suspend fun sumFocusMinutesSince(username: String, since: Long): Int

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE username = :username AND sessionType = 'FOCUS'")
    suspend fun countTotalFocusSessions(username: String): Int

    @Query("SELECT IFNULL(SUM(durationMinutes), 0) FROM focus_sessions WHERE username = :username AND sessionType = 'FOCUS'")
    suspend fun sumTotalFocusMinutes(username: String): Int

    @Query("SELECT * FROM focus_sessions WHERE username = :username AND completedAt >= :since ORDER BY completedAt ASC")
    suspend fun getSessionsSince(username: String, since: Long): List<FocusSessionEntity>

    @Insert
    suspend fun insert(session: FocusSessionEntity): Long
}
