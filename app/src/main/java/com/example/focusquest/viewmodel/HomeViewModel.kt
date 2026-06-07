package com.example.focusquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.focusquest.UserPreferencesManager
import com.example.focusquest.data.api.model.QuoteResponse
import com.example.focusquest.data.db.AppDatabase
import com.example.focusquest.data.repository.ContentRepository
import com.example.focusquest.data.repository.FocusRepository
import com.example.focusquest.data.repository.TaskRepository
import com.example.focusquest.data.repository.WeatherData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = UserPreferencesManager(app)
    private val db = AppDatabase.getInstance(app)
    private val taskRepo = TaskRepository(db.taskDao())
    private val focusRepo = FocusRepository(db.focusSessionDao())
    private val contentRepo = ContentRepository()

    val quote = MutableLiveData<QuoteResponse?>()
    val weather = MutableLiveData<WeatherData?>()
    val bgImageUrl = MutableLiveData<String?>()
    val tasksDoneToday = MutableLiveData(0)
    val sessionsToday = MutableLiveData(0)
    val focusMinutesToday = MutableLiveData(0)
    val isLoadingQuote = MutableLiveData(false)
    val isLoadingWeather = MutableLiveData(false)

    init {
        observeTodayStats()
        loadAll()
    }

    fun loadAll() {
        loadQuote()
        loadWeather()
    }

    fun loadQuote() {
        viewModelScope.launch {
            isLoadingQuote.value = true
            contentRepo.fetchQuote().onSuccess { quote.value = it }
            isLoadingQuote.value = false
        }
        loadBackgroundImage()
    }

    private fun loadBackgroundImage() {
        viewModelScope.launch {
            contentRepo.fetchBackgroundImageUrl().onSuccess { bgImageUrl.value = it }
        }
    }

    private fun loadWeather() {
        viewModelScope.launch {
            isLoadingWeather.value = true
            contentRepo.fetchWeather().onSuccess { weather.value = it }
            isLoadingWeather.value = false
        }
    }

    private fun observeTodayStats() {
        val username = prefs.getCurrentUser()?.username ?: return
        viewModelScope.launch {
            taskRepo.countCompletedTodayFlow(username).collectLatest { count ->
                tasksDoneToday.postValue(count)
            }
        }
        viewModelScope.launch {
            focusRepo.todayFocusSessionsFlow(username).collectLatest { count ->
                sessionsToday.postValue(count)
            }
        }
        viewModelScope.launch {
            focusRepo.todayFocusMinutesFlow(username).collectLatest { minutes ->
                focusMinutesToday.postValue(minutes)
            }
        }
    }
}
