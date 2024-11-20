package com.nikita.weatherappui.di

import com.nikita.weatherappui.api.RetrofitClient
import com.nikita.weatherappui.api.WeatherApiService
import com.nikita.weatherappui.repository.WeatherRepository
import com.nikita.weatherappui.viewmodel.WeatherViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<WeatherApiService> { RetrofitClient.create() }

    single { WeatherRepository(get()) }

    viewModel { WeatherViewModel(get()) }
}