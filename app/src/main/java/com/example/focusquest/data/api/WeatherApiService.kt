package com.example.focusquest.data.api

import com.example.focusquest.data.api.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 1
    ): WeatherResponse
}
