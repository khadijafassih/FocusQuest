package com.example.focusquest.data.api

import com.example.focusquest.data.api.model.QuoteResponse
import retrofit2.http.GET

interface QuoteApiService {
    @GET("api/random")
    suspend fun getRandomQuote(): List<QuoteResponse>
}
