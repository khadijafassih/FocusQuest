package com.example.focusquest.data.api

import com.example.focusquest.data.api.model.LocationResponse
import retrofit2.http.GET

interface LocationApiService {
    @GET("json")
    suspend fun getLocation(): LocationResponse
}
