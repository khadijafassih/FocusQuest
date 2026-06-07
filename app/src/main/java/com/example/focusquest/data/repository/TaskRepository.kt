package com.example.focusquest.data.repository

import com.example.focusquest.data.db.dao.TaskDao
import com.example.focusquest.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(private val dao: TaskDao) {

    fun getAllTasks(username: String) = dao.getAllTasks(username)
    fun getPendingTasks(username: String) = dao.getPendingTasks(username)
    fun getCompletedTasks(username: String) = dao.getCompletedTasks(username)

    suspend fun insert(task: TaskEntity): Long = dao.insert(task)
    suspend fun update(task: TaskEntity) = dao.update(task)
    suspend fun delete(task: TaskEntity) = dao.delete(task)

    suspend fun getPendingSync(username: String) = dao.getPendingTasksSync(username)

    suspend fun countCompletedToday(username: String): Int =
        dao.countCompletedSince(username, startOfDay())

    fun countCompletedTodayFlow(username: String): Flow<Int> =
        dao.countCompletedSinceFlow(username, startOfDay())

    suspend fun countTotalCompleted(username: String): Int =
        dao.countTotalCompleted(username)

    private fun startOfDay(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
