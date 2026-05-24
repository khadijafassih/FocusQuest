package com.example.focusquest.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.focusquest.data.db.dao.AchievementDao
import com.example.focusquest.data.db.dao.FocusSessionDao
import com.example.focusquest.data.db.dao.TaskDao
import com.example.focusquest.data.db.entity.AchievementEntity
import com.example.focusquest.data.db.entity.FocusSessionEntity
import com.example.focusquest.data.db.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, FocusSessionEntity::class, AchievementEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS achievements " +
                    "(id TEXT NOT NULL, username TEXT NOT NULL, unlockedAt INTEGER NOT NULL, " +
                    "PRIMARY KEY(id, username))"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focusquest.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
