package com.example.focusquest.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.focusquest.data.db.entity.TaskEntity

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE username = :username ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getAllTasks(username: String): LiveData<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE username = :username AND isCompleted = 0 ORDER BY priority DESC, dueDate ASC, createdAt DESC")
    fun getPendingTasks(username: String): LiveData<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE username = :username AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(username: String): LiveData<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE username = :username AND isCompleted = 0 LIMIT 10")
    suspend fun getPendingTasksSync(username: String): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE username = :username AND isCompleted = 1 AND completedAt >= :since")
    suspend fun countCompletedSince(username: String, since: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE username = :username AND isCompleted = 1")
    suspend fun countTotalCompleted(username: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}
