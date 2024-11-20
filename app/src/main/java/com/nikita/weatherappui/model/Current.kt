package com.nikita.weatherappui.model

import com.google.gson.annotations.SerializedName

data class Current(
    @SerializedName("temp_c") val tempC: Double,
    @SerializedName("wind_kph") val windKph: Double,
    val humidity: Int,
    val condition: Condition,
    @SerializedName("precip_mm") val precipitationMm: Double
)