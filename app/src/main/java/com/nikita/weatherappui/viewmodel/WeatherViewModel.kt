package com.nikita.weatherappui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikita.weatherappui.model.WeatherResponse
import com.nikita.weatherappui.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _currentWeather = MutableLiveData<WeatherResponse?>()
    val currentWeather: LiveData<WeatherResponse?> = _currentWeather

    private val _forecastWeather = MutableLiveData<WeatherResponse?>()
    val forecastWeather: LiveData<WeatherResponse?> = _forecastWeather

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchTodayWeather(lat: String, lon: String) {
        viewModelScope.launch {
            try {
                val location = "$lat,$lon"
                val weather = repository.getCurrentWeather(location)
                _currentWeather.postValue(weather)
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching today's weather: ${e.message}")
            }
        }
    }

    fun fetchForecast(lat: String, lon: String, days: Int) {
        viewModelScope.launch {
            try {
                val location = "$lat,$lon"
                val forecast = repository.getForecastForDays(location, days)
                _forecastWeather.postValue(forecast)
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching forecast: ${e.message}")
            }
        }
    }
}