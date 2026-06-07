package com.example.focusquest.data.api

import com.example.focusquest.data.api.model.UnsplashResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UnsplashApiService {
    @GET("photos/random")
    suspend fun getRandomPhoto(
        @Header("Authorization") clientId: String,
        @Query("query") query: String = "nature focus minimal",
        @Query("orientation") orientation: String = "landscape"
    ): UnsplashResponse
}
