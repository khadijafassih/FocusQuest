package com.example.focusquest

import com.example.focusquest.data.db.AppDatabase
import com.example.focusquest.data.db.entity.AchievementEntity
import com.example.focusquest.data.db.entity.FocusSessionEntity
import java.util.Calendar

object AchievementManager {

    data class AchievementDef(val id: String, val title: String, val desc: String, val emoji: String)

    val ALL = listOf(
        AchievementDef("FIRST_SESSION", "First Step",      "Complete your first focus session",    "🎯"),
        AchievementDef("SESSIONS_10",   "Getting Serious", "Complete 10 focus sessions",            "📚"),
        AchievementDef("SESSIONS_50",   "Pomodoro Pro",    "Complete 50 focus sessions",            "🍅"),
        AchievementDef("TASK_10",       "Task Master",     "Complete 10 tasks",                     "✅"),
        AchievementDef("TASK_50",       "Century Runner",  "Complete 50 tasks",                     "💯"),
        AchievementDef("HOURS_5",       "Half Day",        "Accumulate 5 hours of focus",           "⏰"),
        AchievementDef("HOURS_10",      "Time Lord",       "Accumulate 10 hours of focus",          "🕐"),
        AchievementDef("STREAK_3",      "On a Roll",       "Focus 3 days in a row",                 "🔥"),
        AchievementDef("STREAK_7",      "Week Warrior",    "Maintain a 7-day focus streak",         "⚡"),
        AchievementDef("DEEP_DIVER",    "Deep Diver",      "Complete a Deep Focus (50m) session",   "🌊"),
        AchievementDef("ULTRA_BEAST",   "Ultra Beast",     "Complete an Ultra Focus (90m) session", "🦁"),
        AchievementDef("THREE_TODAY",   "Triple Threat",   "Complete 3 sessions in one day",        "🏆")
    )

    suspend fun checkAndUnlock(username: String, db: AppDatabase): List<AchievementDef> {
        val sessionDao = db.focusSessionDao()
        val taskDao    = db.taskDao()
        val achDao     = db.achievementDao()

        val unlocked = achDao.getUnlockedIds(username).toSet()
        val newOnes  = mutableListOf<AchievementDef>()

        val totalSessions = sessionDao.countTotalFocusSessions(username)
        val totalMinutes  = sessionDao.sumTotalFocusMinutes(username)
        val totalTasks    = taskDao.countTotalCompleted(username)
        val todayStart    = startOfDay()
        val todaySessions = sessionDao.countFocusSessionsSince(username, todayStart)

        val allSessions  = sessionDao.getAllFocusSessions(username)
        val streak       = calcStreak(allSessions)
        val hasDeepEver  = allSessions.any { it.durationMinutes >= 50 && it.sessionType == "FOCUS" }
        val hasUltraEver = allSessions.any { it.durationMinutes >= 90 && it.sessionType == "FOCUS" }

        val checks = mapOf(
            "FIRST_SESSION" to (totalSessions >= 1),
            "SESSIONS_10"   to (totalSessions >= 10),
            "SESSIONS_50"   to (totalSessions >= 50),
            "TASK_10"       to (totalTasks >= 10),
            "TASK_50"       to (totalTasks >= 50),
            "HOURS_5"       to (totalMinutes >= 300),
            "HOURS_10"      to (totalMinutes >= 600),
            "STREAK_3"      to (streak >= 3),
            "STREAK_7"      to (streak >= 7),
            "DEEP_DIVER"    to hasDeepEver,
            "ULTRA_BEAST"   to hasUltraEver,
            "THREE_TODAY"   to (todaySessions >= 3)
        )

        for ((id, satisfied) in checks) {
            if (id !in unlocked && satisfied) {
                achDao.insert(AchievementEntity(id, username))
                ALL.find { it.id == id }?.let { newOnes.add(it) }
            }
        }
        return newOnes
    }

    private fun startOfDay() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun calcStreak(sessions: List<FocusSessionEntity>): Int {
        if (sessions.isEmpty()) return 0
        val dayCal = Calendar.getInstance()
        val today  = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val dayMs = 24 * 60 * 60 * 1000L
        val activeDays = sessions.map { s ->
            dayCal.timeInMillis = s.completedAt
            dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSortedSet().toList().reversed()

        var streak = 0; var check = today.timeInMillis
        for (day in activeDays) {
            if (day == check || day == check - dayMs) { streak++; check = day }
            else if (day < check - dayMs) break
        }
        return streak
    }
}
