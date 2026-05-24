package com.example.focusquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.focusquest.UserPreferencesManager
import com.example.focusquest.data.db.AppDatabase
import com.example.focusquest.data.repository.FocusRepository
import com.example.focusquest.data.repository.TaskRepository
import kotlinx.coroutines.launch

class StatsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = UserPreferencesManager(app)
    private val focusRepo = FocusRepository(AppDatabase.getInstance(app).focusSessionDao())
    private val taskRepo = TaskRepository(AppDatabase.getInstance(app).taskDao())

    val weeklyHours = MutableLiveData<List<Float>>()
    val totalSessions = MutableLiveData(0)
    val totalHours = MutableLiveData(0f)
    val totalTasksCompleted = MutableLiveData(0)
    val currentStreak = MutableLiveData(0)

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            val username = prefs.getCurrentUser()?.username ?: return@launch
            weeklyHours.value = focusRepo.getWeeklyHours(username)
            totalSessions.value = focusRepo.totalSessions(username)
            totalHours.value = focusRepo.totalMinutes(username) / 60f
            totalTasksCompleted.value = taskRepo.countTotalCompleted(username)
            currentStreak.value = focusRepo.currentStreak(username)
        }
    }
}
