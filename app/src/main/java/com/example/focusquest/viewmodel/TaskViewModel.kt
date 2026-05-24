package com.example.focusquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.focusquest.AchievementManager
import com.example.focusquest.UserPreferencesManager
import com.example.focusquest.data.db.AppDatabase
import com.example.focusquest.data.db.entity.TaskEntity
import com.example.focusquest.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = UserPreferencesManager(app)
    private val db = AppDatabase.getInstance(app)
    private val repo = TaskRepository(db.taskDao())

    val newAchievements = MutableLiveData<List<AchievementManager.AchievementDef>>(emptyList())

    val username: String get() = prefs.getCurrentUser()?.username ?: ""

    fun allTasks() = repo.getAllTasks(username)
    fun pendingTasks() = repo.getPendingTasks(username)
    fun completedTasks() = repo.getCompletedTasks(username)

    fun add(task: TaskEntity) = viewModelScope.launch {
        repo.insert(task)
    }

    fun update(task: TaskEntity) = viewModelScope.launch {
        repo.update(task)
    }

    fun complete(task: TaskEntity) = viewModelScope.launch {
        repo.update(task.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
        prefs.updateUserXP(task.xpReward)
        val username = prefs.getCurrentUser()?.username ?: return@launch
        val unlocked = AchievementManager.checkAndUnlock(username, db)
        if (unlocked.isNotEmpty()) newAchievements.postValue(unlocked)
    }

    fun acknowledgeAchievements() { newAchievements.value = emptyList() }

    fun uncomplete(task: TaskEntity) = viewModelScope.launch {
        repo.update(task.copy(isCompleted = false, completedAt = null))
    }

    fun delete(task: TaskEntity) = viewModelScope.launch {
        repo.delete(task)
    }

    suspend fun getPendingSync() = repo.getPendingSync(username)
}
