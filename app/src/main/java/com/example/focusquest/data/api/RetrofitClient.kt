package com.example.focusquest.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Unsplash Access Key (read-only, safe for client-side use)
    const val UNSPLASH_ACCESS_KEY = "rWpjHlaDTGOeTNFNSXoBdwLiPOrN0WsXtTSgpkIAlsU"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private fun buildRetrofit(baseUrl: String) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val quoteApi: QuoteApiService =
        buildRetrofit("https://zenquotes.io/").create(QuoteApiService::class.java)

    val weatherApi: WeatherApiService =
        buildRetrofit("https://api.open-meteo.com/").create(WeatherApiService::class.java)

    val locationApi: LocationApiService =
        buildRetrofit("https://ipapi.co/").create(LocationApiService::class.java)

    val unsplashApi: UnsplashApiService =
        buildRetrofit("https://api.unsplash.com/").create(UnsplashApiService::class.java)
}
