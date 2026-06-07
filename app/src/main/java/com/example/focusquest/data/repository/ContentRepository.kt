package com.example.focusquest.data.repository

import com.example.focusquest.data.api.RetrofitClient
import com.example.focusquest.data.api.model.QuoteResponse

data class WeatherData(
    val city: String,
    val temperature: Int,
    val condition: String,
    val emoji: String,
    val humidity: Int
)

class ContentRepository {

    suspend fun fetchQuote(): Result<QuoteResponse> = runCatching {
        RetrofitClient.quoteApi.getRandomQuote().first()
    }

    suspend fun fetchWeather(): Result<WeatherData> = runCatching {
        val loc = RetrofitClient.locationApi.getLocation()
        val weather = RetrofitClient.weatherApi.getCurrentWeather(loc.lat, loc.lon)
        WeatherData(
            city = loc.city.ifBlank { loc.country },
            temperature = weather.current.temperature.toInt(),
            condition = wmoCondition(weather.current.weatherCode),
            emoji = wmoEmoji(weather.current.weatherCode),
            humidity = weather.current.humidity
        )
    }

    suspend fun fetchBackgroundImageUrl(query: String = "nature focus"): Result<String> = runCatching {
        val key = RetrofitClient.UNSPLASH_ACCESS_KEY
        if (key == "YOUR_UNSPLASH_ACCESS_KEY") error("Unsplash key not set")
        RetrofitClient.unsplashApi
            .getRandomPhoto(clientId = "Client-ID $key", query = query)
            .urls.regular
    }

    private fun wmoCondition(code: Int) = when (code) {
        0 -> "Clear Sky"
        1, 2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Raining"
        71, 73, 75 -> "Snowing"
        80, 81, 82 -> "Rain Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Hail Storm"
        else -> "Cloudy"
    }

    private fun wmoEmoji(code: Int) = when (code) {
        0 -> "☀️"
        1, 2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
        71, 73, 75 -> "❄️"
        95, 96, 99 -> "⛈️"
        else -> "🌤️"
    }
}
