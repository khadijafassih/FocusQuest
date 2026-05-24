package com.example.focusquest

import android.content.Context
import com.google.gson.Gson

class UserPreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("FocusQuestAuth", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREF_KEY_USER = "current_user"
        private const val PREF_KEY_IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * Register a new user
     */
    fun registerUser(user: User): Boolean {
        // Check if user already exists
        if (getUserByUsername(user.username) != null) {
            return false // User already exists
        }
        
        // Store user data
        val userJson = gson.toJson(user)
        val usersJson = sharedPreferences.getString("all_users", "[]")
        val userList = gson.fromJson(usersJson, Array<User>::class.java).toMutableList()
        userList.add(user)
        
        sharedPreferences.edit()
            .putString("all_users", gson.toJson(userList))
            .apply()
        
        return true
    }

    /**
     * Login user with username and password
     */
    fun loginUser(username: String, password: String): Boolean {
        val user = getUserByUsername(username)
        if (user != null && user.password == password) {
            saveCurrentUser(user)
            return true
        }
        return false
    }

    /**
     * Get user by username
     */
    private fun getUserByUsername(username: String): User? {
        val usersJson = sharedPreferences.getString("all_users", "[]")
        val userList = gson.fromJson(usersJson, Array<User>::class.java).toList()
        return userList.find { it.username == username }
    }

    /**
     * Save current logged-in user
     */
    private fun saveCurrentUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit()
            .putString(PREF_KEY_USER, userJson)
            .putBoolean(PREF_KEY_IS_LOGGED_IN, true)
            .apply()
    }

    /**
     * Get current logged-in user
     */
    fun getCurrentUser(): User? {
        val userJson = sharedPreferences.getString(PREF_KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(PREF_KEY_IS_LOGGED_IN, false)
    }

    /**
     * Logout user
     */
    fun logoutUser() {
        sharedPreferences.edit()
            .putBoolean(PREF_KEY_IS_LOGGED_IN, false)
            .putString(PREF_KEY_USER, null)
            .apply()
    }

    /**
     * Update current user XP
     */
    fun updateUserXP(xpAmount: Int) {
        val user = getCurrentUser()
        if (user != null) {
            user.xp = (user.xp + xpAmount).coerceAtLeast(0)
            user.level = (user.xp / 100) + 1
            
            // Update in all users list
            val usersJson = sharedPreferences.getString("all_users", "[]")
            val userList = gson.fromJson(usersJson, Array<User>::class.java).toMutableList()
            val index = userList.indexOfFirst { it.username == user.username }
            if (index >= 0) {
                userList[index] = user
            }
            
            sharedPreferences.edit()
                .putString("all_users", gson.toJson(userList))
                .putString(PREF_KEY_USER, gson.toJson(user))
                .apply()
        }
    }

    /**
     * Set current user XP to an exact value
     */
    fun setCurrentUserXP(xp: Int) {
        val user = getCurrentUser() ?: return
        user.xp = xp.coerceAtLeast(0)
        user.level = (user.xp / 100) + 1

        val usersJson = sharedPreferences.getString("all_users", "[]")
        val userList = gson.fromJson(usersJson, Array<User>::class.java).toMutableList()
        val index = userList.indexOfFirst { it.username == user.username }
        if (index >= 0) {
            userList[index] = user
        }

        sharedPreferences.edit()
            .putString("all_users", gson.toJson(userList))
            .putString(PREF_KEY_USER, gson.toJson(user))
            .apply()
    }

    /**
     * Build per-user storage key for task list
     */
    fun getCurrentUserTasksKey(): String {
        val username = getCurrentUser()?.username ?: "guest"
        return "tasks_$username"
    }

    /**
     * Update profile image
     */
    fun updateProfileImage(imageUri: String) {
        val user = getCurrentUser()
        if (user != null) {
            user.profileImage = imageUri
            
            // Update in all users list
            val usersJson = sharedPreferences.getString("all_users", "[]")
            val userList = gson.fromJson(usersJson, Array<User>::class.java).toMutableList()
            val index = userList.indexOfFirst { it.username == user.username }
            if (index >= 0) {
                userList[index] = user
            }
            
            sharedPreferences.edit()
                .putString("all_users", gson.toJson(userList))
                .putString(PREF_KEY_USER, gson.toJson(user))
                .apply()
        }
    }

    /**
     * Clear current user's profile image when a saved URI becomes invalid
     */
    fun clearCurrentUserProfileImage() {
        val user = getCurrentUser() ?: return
        user.profileImage = null

        val usersJson = sharedPreferences.getString("all_users", "[]")
        val userList = gson.fromJson(usersJson, Array<User>::class.java).toMutableList()
        val index = userList.indexOfFirst { it.username == user.username }
        if (index >= 0) {
            userList[index] = user
        }

        sharedPreferences.edit()
            .putString("all_users", gson.toJson(userList))
            .putString(PREF_KEY_USER, gson.toJson(user))
            .apply()
    }

    // ── Feature 4: Custom Timer Durations ──────────────────────────────────
    fun getCustomDuration(type: com.example.focusquest.viewmodel.SessionType): Int? {
        val key = "custom_duration_${type.name}"
        val v = sharedPreferences.getInt(key, -1)
        return if (v == -1) null else v
    }

    fun setCustomDuration(type: com.example.focusquest.viewmodel.SessionType, minutes: Int) {
        sharedPreferences.edit().putInt("custom_duration_${type.name}", minutes).apply()
    }

    // ── Feature 5: Scheduled Reminders ────────────────────────────────────
    fun getReminderEnabled(): Boolean = sharedPreferences.getBoolean("reminder_enabled", false)
    fun setReminderEnabled(v: Boolean) = sharedPreferences.edit().putBoolean("reminder_enabled", v).apply()
    fun getReminderHour(): Int = sharedPreferences.getInt("reminder_hour", 9)
    fun getReminderMinute(): Int = sharedPreferences.getInt("reminder_minute", 0)
    fun setReminderTime(hour: Int, minute: Int) = sharedPreferences.edit()
        .putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()

    // ── Feature 6: Dark Mode ───────────────────────────────────────────────
    fun getDarkMode(): Int = sharedPreferences.getInt("dark_mode", 0) // 0=system,1=light,2=dark
    fun setDarkMode(mode: Int) = sharedPreferences.edit().putInt("dark_mode", mode).apply()

    // ── Feature 8: Onboarding & Daily Goal ───────────────────────────────
    fun hasSeenOnboarding(): Boolean = sharedPreferences.getBoolean("has_seen_onboarding", false)
    fun setHasSeenOnboarding(seen: Boolean) = sharedPreferences.edit().putBoolean("has_seen_onboarding", seen).apply()
    fun getDailyGoal(): Int = sharedPreferences.getInt("daily_goal", 4)
    fun setDailyGoal(goal: Int) = sharedPreferences.edit().putInt("daily_goal", goal).apply()
}
