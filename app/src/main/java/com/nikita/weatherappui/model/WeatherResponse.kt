package com.nikita.weatherappui.model

data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)