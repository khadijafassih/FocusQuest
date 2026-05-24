package com.example.focusquest.data.api.model

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("latitude") val lat: Double,
    @SerializedName("longitude") val lon: Double,
    @SerializedName("city") val city: String,
    @SerializedName("country_name") val country: String,
    @SerializedName("error") val error: Boolean = false
)
