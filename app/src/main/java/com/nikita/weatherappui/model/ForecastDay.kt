package com.nikita.weatherappui.model

import Hour

data class ForecastDay(
    val date: String,
    val day: Day,
    val hour: List<Hour>
)