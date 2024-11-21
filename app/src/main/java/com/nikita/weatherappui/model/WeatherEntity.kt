package com.nikita.weatherappui.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val location: String,
    val weatherData: String,
    val timestamp: Long
)