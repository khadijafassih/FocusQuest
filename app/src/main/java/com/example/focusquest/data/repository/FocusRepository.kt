package com.example.focusquest.data.repository

import com.example.focusquest.data.db.dao.FocusSessionDao
import com.example.focusquest.data.db.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FocusRepository(private val dao: FocusSessionDao) {

    fun getRecentSessions(username: String) = dao.getRecentSessions(username)

    suspend fun insert(session: FocusSessionEntity) = dao.insert(session)

    suspend fun todayFocusSessions(username: String): Int =
        dao.countFocusSessionsSince(username, startOfDay())

    suspend fun todayFocusMinutes(username: String): Int =
        dao.sumFocusMinutesSince(username, startOfDay())

    fun todayFocusSessionsFlow(username: String): Flow<Int> =
        dao.countFocusSessionsSinceFlow(username, startOfDay())

    fun todayFocusMinutesFlow(username: String): Flow<Int> =
        dao.sumFocusMinutesSinceFlow(username, startOfDay())

    suspend fun totalSessions(username: String): Int =
        dao.countTotalFocusSessions(username)

    suspend fun totalMinutes(username: String): Int =
        dao.sumTotalFocusMinutes(username)

    suspend fun getWeeklyHours(username: String): List<Float> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis
        val sessions = dao.getSessionsSince(username, weekStart)

        val dayMap = FloatArray(7)
        val dayCal = Calendar.getInstance()
        for (s in sessions) {
            if (s.sessionType == FocusSessionEntity.TYPE_FOCUS) {
                dayCal.timeInMillis = s.completedAt
                val dayIndex = ((dayCal.get(Calendar.DAY_OF_WEEK) - cal.firstDayOfWeek) + 7) % 7
                dayMap[dayIndex] += s.durationMinutes / 60f
            }
        }
        return dayMap.toList()
    }

    suspend fun currentStreak(username: String): Int {
        val sessions = dao.getAllFocusSessions(username)
        if (sessions.isEmpty()) return 0

        val dayCal = Calendar.getInstance()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val activeDays = sessions.map { s ->
            dayCal.timeInMillis = s.completedAt
            dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSortedSet().toList().reversed()

        var streak = 0
        var checkDay = today.timeInMillis
        val dayMs = 24 * 60 * 60 * 1000L

        for (day in activeDays) {
            if (day == checkDay || day == checkDay - dayMs) {
                streak++
                checkDay = day
            } else if (day < checkDay - dayMs) {
                break
            }
        }
        return streak
    }

    private fun startOfDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
