package com.example.focusquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.focusquest.UserPreferencesManager
import com.example.focusquest.data.db.AppDatabase
import com.example.focusquest.data.db.entity.TaskEntity
import com.example.focusquest.network.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(val text: String, val isUser: Boolean)

data class AiTask(val title: String, val priority: String, val description: String)

data class AiPlanResponse(val projectName: String?, val tasks: List<AiTask>)

class AiPlannerViewModel(app: Application) : AndroidViewModel(app) {

    val messages     = MutableLiveData<List<ChatMessage>>(emptyList())
    val parsedTasks  = MutableLiveData<List<AiTask>>(emptyList())
    val projectName  = MutableLiveData<String>("My Project")
    val isLoading    = MutableLiveData(false)
    val tasksCreated = MutableLiveData(false)
    val errorEvent   = MutableLiveData<String?>(null)

    private val prefs = UserPreferencesManager(app)
    private val db    = AppDatabase.getInstance(app)
    private val api   = GroqApiService.create()
    private val gson  = Gson()

    private val systemMessage = GroqMessage(
        role = "system",
        content = """
            You are FocusQuest AI, a productivity coach inside a focus and task management app.
            Help users break down projects into actionable tasks.

            Follow this STRICT two-phase process:

            PHASE 1 — When the user first describes a project, ask exactly 2 short numbered
            clarifying questions about deadline, scope, priority level, or key constraints.
            Do NOT generate tasks in Phase 1.

            PHASE 2 — After the user answers, write one brief summary sentence, then output
            the task list in this EXACT format:

            ```json
            {
              "projectName": "Short Project Name",
              "tasks": [{"title":"Task title","priority":"HIGH","description":"One sentence description."}]
            }
            ```

            Rules: 4-8 tasks, priority must be HIGH/MEDIUM/LOW, titles under 55 chars,
            one-sentence descriptions, order tasks logically.
        """.trimIndent()
    )

    private val history = mutableListOf(systemMessage)

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val updated = messages.value.orEmpty().toMutableList()
        updated.add(ChatMessage(trimmed, isUser = true))
        messages.value = updated
        isLoading.value = true

        history.add(GroqMessage("user", trimmed))

        viewModelScope.launch {
            try {
                val request = GroqRequest(messages = trimmedHistory())
                val response = generateWithRetry(request)
                val replyText = response.choices
                    ?.firstOrNull()?.message?.content
                    ?: "Sorry, I couldn't generate a response. Please try again."

                history.add(GroqMessage("assistant", replyText))

                val plan = extractPlan(replyText)
                val displayText = if (plan != null && plan.tasks.isNotEmpty()) {
                    replyText.substringBefore("```json").trim()
                } else {
                    replyText
                }

                val list = messages.value.orEmpty().toMutableList()
                list.add(ChatMessage(displayText, isUser = false))
                messages.value = list

                plan?.let {
                    if (it.tasks.isNotEmpty()) {
                        parsedTasks.value = it.tasks
                        it.projectName?.let { name -> projectName.value = name }
                    }
                }

            } catch (e: retrofit2.HttpException) {
                val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                val msg = when {
                    e.code() == 401 ->
                        "Invalid API key. Please check your Groq API key."
                    e.code() == 400 ->
                        "Bad request. The API key may be missing or malformed."
                    e.code() == 403 ->
                        "Access denied. API key may be restricted."
                    e.code() == 429 ->
                        "Rate limit reached — retries exhausted. Please wait a moment and try again."
                    e.code() in listOf(500, 503) ->
                        "Groq service unavailable. Please try again shortly."
                    else ->
                        "API error (${e.code()}). Please try again."
                }
                addErrorMessage(msg)
                history.removeLastOrNull()
            } catch (e: Exception) {
                addErrorMessage("Network error: ${e.message}. Check your internet connection.")
                history.removeLastOrNull()
            } finally {
                isLoading.value = false
            }
        }
    }

    private suspend fun generateWithRetry(request: GroqRequest, maxRetries: Int = 3): GroqResponse {
        var lastEx: retrofit2.HttpException? = null
        for (attempt in 0 until maxRetries) {
            try {
                return withContext(Dispatchers.IO) {
                    api.generate("Bearer ${GroqApiService.API_KEY}", request)
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() != 429) throw e
                lastEx = e
                delay((1L shl (attempt + 1)) * 1000L) // 2s, 4s, 8s
            }
        }
        throw lastEx!!
    }

    private fun trimmedHistory(): List<GroqMessage> {
        val system = history.take(1)
        val recent = history.drop(1).takeLast(6)
        return system + recent
    }

    private fun addErrorMessage(msg: String) {
        val list = messages.value.orEmpty().toMutableList()
        list.add(ChatMessage(msg, isUser = false))
        messages.value = list
    }

    private fun extractPlan(text: String): AiPlanResponse? {
        val match = Regex("```json\\s*(\\{[\\s\\S]*?\\})\\s*```").find(text) ?: return null
        val json = match.groupValues[1].trim()
        return try {
            gson.fromJson(json, AiPlanResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun createTasks() {
        val tasks = parsedTasks.value ?: return
        val projName = projectName.value ?: "Project"
        val username = prefs.getCurrentUser()?.username ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tasks.forEachIndexed { index, aiTask ->
                    val priority = when (aiTask.priority.uppercase()) {
                        "HIGH" -> TaskEntity.PRIORITY_HIGH
                        "LOW"  -> TaskEntity.PRIORITY_LOW
                        else   -> TaskEntity.PRIORITY_MEDIUM
                    }
                    val labeledTitle = "[$projName] ${index + 1}: ${aiTask.title}"
                    db.taskDao().insert(
                        TaskEntity(
                            username    = username,
                            title       = labeledTitle,
                            description = aiTask.description,
                            category    = "Work",
                            priority    = priority,
                            xpReward    = TaskEntity.xpForCategory("Work", priority)
                        )
                    )
                }
            }
            tasksCreated.postValue(true)
        }
    }

    fun resetSession() {
        messages.value = emptyList()
        parsedTasks.value = emptyList()
        projectName.value = "My Project"
        history.clear()
        history.add(systemMessage)
        tasksCreated.value = false
    }

    fun acknowledgeTasksCreated() { tasksCreated.value = false }
    fun clearPlan() { parsedTasks.value = emptyList() }
}
