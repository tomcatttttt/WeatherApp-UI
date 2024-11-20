package com.nikita.weatherappui.model

import com.google.gson.annotations.SerializedName

data class Day(
    @SerializedName("maxtemp_c") val maxTempC: Double,
    @SerializedName("mintemp_c") val minTempC: Double,
    @SerializedName("condition") val condition: Condition
)