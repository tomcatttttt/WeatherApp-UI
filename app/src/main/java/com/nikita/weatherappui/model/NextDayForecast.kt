package com.nikita.weatherappui.model

data class NextDayForecast(
    val dayOfWeek: String,
    val maxTemp: Int,
    val minTemp: Int,
    val iconUrl: String
)