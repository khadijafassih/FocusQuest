package com.example.focusquest.data.api.model

import com.google.gson.annotations.SerializedName

data class UnsplashResponse(
    @SerializedName("urls") val urls: UnsplashUrls,
    @SerializedName("alt_description") val description: String?
)

data class UnsplashUrls(
    @SerializedName("regular") val regular: String,
    @SerializedName("small") val small: String
)
