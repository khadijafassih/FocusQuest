package com.example.focusquest.network

import com.example.focusquest.BuildConfig
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// ── Request models ────────────────────────────────────────────────────────────

data class GroqMessage(val role: String, val content: String)

data class GroqRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 1024
)

// ── Response models ───────────────────────────────────────────────────────────

data class GroqChoice(val message: GroqMessage?)

data class GroqResponse(val choices: List<GroqChoice>?)

// ── Retrofit interface ────────────────────────────────────────────────────────

interface GroqApiService {

    @POST("chat/completions")
    suspend fun generate(
        @Header("Authorization") auth: String,
        @Body body: GroqRequest
    ): GroqResponse

    companion object {
        val API_KEY get() = BuildConfig.GROQ_API_KEY

        fun create(): GroqApiService = Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }
}
