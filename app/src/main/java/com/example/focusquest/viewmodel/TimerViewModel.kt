package com.example.focusquest.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.focusquest.AchievementManager
import com.example.focusquest.TimerService
import com.example.focusquest.UserPreferencesManager
import com.example.focusquest.data.db.AppDatabase
import com.example.focusquest.data.db.entity.TaskEntity
import com.example.focusquest.data.repository.FocusRepository
import kotlinx.coroutines.launch

enum class SessionType(val label: String, val minutes: Int, val xp: Int) {
    POMODORO("Pomodoro", 25, 25),
    DEEP_FOCUS("Deep Focus", 50, 50),
    ULTRA_FOCUS("Ultra Focus", 90, 90),
    SHORT_BREAK("Short Break", 5, 0),
    LONG_BREAK("Long Break", 15, 0)
}

enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }

class TimerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = UserPreferencesManager(app)
    private val db = AppDatabase.getInstance(app)
    private val focusRepo = FocusRepository(db.focusSessionDao())


    val sessionType              = MutableLiveData(SessionType.POMODORO)
    val timerState               = MutableLiveData(TimerState.IDLE)
    val timeRemainingMs          = MutableLiveData(SessionType.POMODORO.minutes * 60_000L)
    val progressPercent          = MutableLiveData(0)
    val linkedTask               = MutableLiveData<TaskEntity?>(null)
    val sessionCompleted         = MutableLiveData(false)
    val lastXpEarned             = MutableLiveData(0)
    val completedSessionsToday   = MutableLiveData(0)
    val newAchievements          = MutableLiveData<List<AchievementManager.AchievementDef>>(emptyList())

    private var service: TimerService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as TimerService.TimerBinder).getService()
            service!!.onTick = { ms, pct ->
                timeRemainingMs.postValue(ms)
                progressPercent.postValue(pct)
            }
            service!!.onFinish = { xp, achievements -> onServiceFinished(xp, achievements) }
            service!!.onStateChange = { state -> timerState.postValue(state) }
            // Restore UI state if service was already running
            sessionType.postValue(service!!.sessionType)
            timerState.postValue(service!!.timerState)
            timeRemainingMs.postValue(service!!.timeRemainingMs)
            refreshTodayCount()
        }

        override fun onServiceDisconnected(name: ComponentName?) { service = null }
    }

    init {
        val svcIntent = Intent(app, TimerService::class.java)
        app.startService(svcIntent)
        app.bindService(svcIntent, connection, Context.BIND_AUTO_CREATE)
        refreshTodayCount()
    }

    fun selectType(type: SessionType) {
        val customMin = prefs.getCustomDuration(type)
        service?.selectType(type, customMin)
        sessionType.value = type
        val min = customMin ?: type.minutes
        timeRemainingMs.value = min * 60_000L
        progressPercent.value = 0
    }

    fun setLinkedTask(task: TaskEntity?) {
        linkedTask.value = task
        service?.linkedTaskId    = task?.id
        service?.linkedTaskTitle = task?.title
    }

    fun toggleStartPause() = when (service?.timerState) {
        TimerState.IDLE, TimerState.PAUSED -> service?.startTimer()
        TimerState.RUNNING -> service?.pauseTimer()
        else -> Unit
    }

    fun reset() {
        service?.resetTimer()
        val type = sessionType.value ?: SessionType.POMODORO
        val min = prefs.getCustomDuration(type) ?: type.minutes
        timeRemainingMs.value = min * 60_000L
        progressPercent.value = 0
        timerState.value = TimerState.IDLE
        sessionCompleted.value = false
    }

    fun setSound(name: String?) { service?.setSound(name) }

    private fun onServiceFinished(xpEarned: Int, achievements: List<AchievementManager.AchievementDef>) {
        lastXpEarned.postValue(xpEarned)
        if (achievements.isNotEmpty()) newAchievements.postValue(achievements)
        refreshTodayCount()
        sessionCompleted.postValue(true)
    }

    fun acknowledgeCompletion() { sessionCompleted.value = false }
    fun acknowledgeAchievements() { newAchievements.value = emptyList() }

    private fun refreshTodayCount() {
        viewModelScope.launch {
            val username = prefs.getCurrentUser()?.username ?: return@launch
            completedSessionsToday.postValue(focusRepo.todayFocusSessions(username))
        }
    }

    override fun onCleared() {
        super.onCleared()
        service?.onTick = null
        service?.onFinish = null
        service?.onStateChange = null
        try { getApplication<Application>().unbindService(connection) } catch (_: Exception) {}
    }
}
