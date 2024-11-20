package com.nikita.weatherappui.repository

import com.nikita.weatherappui.BuildConfig
import com.nikita.weatherappui.api.WeatherApiService
import com.nikita.weatherappui.model.WeatherResponse

class WeatherRepository(private val apiService: WeatherApiService) {

    suspend fun getCurrentWeather(location: String): WeatherResponse? {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=${BuildConfig.API_KEY}&q=$location&days=1"
        return try {
            val response = apiService.getForecast(BuildConfig.API_KEY, location, days = 1)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getForecastForDays(location: String, days: Int): WeatherResponse? {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=${BuildConfig.API_KEY}&q=$location&days=$days"
        return try {
            val response = apiService.getForecast(BuildConfig.API_KEY, location, days)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
}