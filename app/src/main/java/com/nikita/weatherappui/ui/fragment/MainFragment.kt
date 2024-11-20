package com.nikita.weatherappui.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.gms.location.LocationServices
import com.nikita.weatherappui.databinding.FragmentMainBinding
import com.nikita.weatherappui.model.HourlyForecast
import com.nikita.weatherappui.model.NextDayForecast
import com.nikita.weatherappui.ui.adapter.HourlyForecastAdapter
import com.nikita.weatherappui.ui.adapter.NextDaysForecastAdapter
import com.nikita.weatherappui.utils.AnimationHelper
import com.nikita.weatherappui.utils.DateUtils
import com.nikita.weatherappui.utils.LocationManager
import com.nikita.weatherappui.utils.PermissionHandler
import com.nikita.weatherappui.viewmodel.WeatherViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val weatherViewModel: WeatherViewModel by viewModel()

    private lateinit var permissionHandler: PermissionHandler
    private lateinit var locationManager: LocationManager
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var nextDaysForecastAdapter: NextDaysForecastAdapter

    private var cityTime: String = ""
    private var isNextForecastVisible = false

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchUserLocation()
            } else {
                fetchDefaultWeather()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        setupUI()
        handleNavigationArguments()
    }

    private fun setupDependencies() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        permissionHandler = PermissionHandler(requireContext(), requestLocationPermissionLauncher)
        locationManager = LocationManager(fusedLocationProviderClient)
    }

    private fun setupUI() {
        setupRecyclerView()
        setupObservers()
        setupToggleForecastButton()
    }

    private fun handleNavigationArguments() {
        val sourceScreen = arguments?.getString("sourceScreen") ?: "unknown"
        if (sourceScreen == "splash") {
            permissionHandler.checkAndRequestLocationPermission(
                onPermissionGranted = { fetchUserLocation() },
                onPermissionDenied = { fetchDefaultWeather() }
            )
        } else {
            fetchDefaultWeather()
        }
    }

    private fun fetchUserLocation() {
        locationManager.fetchLocation(
            onSuccess = { location ->
                fetchTodayWeather(lat = location.latitude.toString(), lon = location.longitude.toString())
                fetchWeeklyForecast(lat = location.latitude.toString(), lon = location.longitude.toString())
            },
            onFailure = { fetchDefaultWeather() }
        )
    }

    private fun fetchDefaultWeather() {
        fetchTodayWeather(lat = "50.4501", lon = "30.5234")
        fetchWeeklyForecast(lat = "50.4501", lon = "30.5234")
    }

    private fun fetchTodayWeather(lat: String, lon: String) {
        weatherViewModel.fetchTodayWeather(lat, lon)
    }

    private fun fetchWeeklyForecast(lat: String, lon: String) {
        weatherViewModel.fetchForecast(lat, lon, days = 7)
    }

    private fun setupRecyclerView() {
        hourlyForecastAdapter = HourlyForecastAdapter(emptyList(), cityTime)
        binding.includeDailyForecast.recyclerViewHourlyForecast.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyForecastAdapter
        }

        nextDaysForecastAdapter = NextDaysForecastAdapter(emptyList())
        binding.includeDailyForecast.recyclerViewNextDaysForecast.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = nextDaysForecastAdapter
        }
    }

    private fun setupToggleForecastButton() {
        binding.nextForecastButton.setOnClickListener {
            toggleForecastContainers()
        }
    }

    private fun toggleForecastContainers() {
        val todayForecastContainer = binding.includeDailyForecast.todayForecastContainer
        val nextDaysForecastContainer = binding.includeDailyForecast.nextDaysForecastContainer

        if (isNextForecastVisible) {
            AnimationHelper.animateVisibility(todayForecastContainer, View.VISIBLE)
            AnimationHelper.animateVisibility(nextDaysForecastContainer, View.GONE)
            binding.nextForecastButton.text = "Next Forecast"
        } else {
            AnimationHelper.animateVisibility(todayForecastContainer, View.GONE)
            AnimationHelper.animateVisibility(nextDaysForecastContainer, View.VISIBLE)
            binding.nextForecastButton.text = "Home"
        }

        isNextForecastVisible = !isNextForecastVisible
    }

    private fun setupObservers() {
        weatherViewModel.currentWeather.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                binding.locationText.text = weather.location.name
                binding.temperatureText.text = "${weather.current.tempC.roundToInt()}°"
                binding.precipitationInfo.text = weather.current.condition.text
                binding.weatherIcon.load("https:${weather.current.condition.icon}")

                cityTime = weather.location.localtime.substringAfter(" ")
                binding.includeDailyForecast.dateText.text = DateUtils.formatDate(weather.location.localtime)

                val forecastToday = weather.forecast.forecastday.firstOrNull()
                forecastToday?.let {
                    binding.maxMinTemp.text =
                        "Max.: ${it.day.maxTempC.roundToInt()}° Min.: ${it.day.minTempC.roundToInt()}°"
                }

                val hourlyForecast = forecastToday?.hour?.map {
                    HourlyForecast(
                        temperature = it.tempC.roundToInt(),
                        iconUrl = "https:${it.condition.icon}",
                        time = it.time.substringAfter(" ")
                    )
                } ?: emptyList()
                hourlyForecastAdapter.updateData(hourlyForecast, cityTime)

                scrollToCurrentHour(hourlyForecast)
                updateWeatherStats(
                    precipitation = weather.current.precipitationMm,
                    humidity = weather.current.humidity,
                    windSpeed = weather.current.windKph
                )
            } else {
                binding.errorMessageText.apply {
                    visibility = View.VISIBLE
                    text = "Failed to load today's weather data."
                }
            }
        }

        weatherViewModel.forecastWeather.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                val nextDaysForecast = weather.forecast.forecastday.map { day ->
                    NextDayForecast(
                        dayOfWeek = DateUtils.formatDate(day.date),
                        maxTemp = day.day.maxTempC.roundToInt(),
                        minTemp = day.day.minTempC.roundToInt(),
                        iconUrl = "https:${day.day.condition.icon}"
                    )
                }
                nextDaysForecastAdapter.updateData(nextDaysForecast)
            } else {
                binding.errorMessageText.apply {
                    visibility = View.VISIBLE
                    text = "Failed to load weekly forecast data."
                }
            }
        }

        weatherViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            binding.errorMessageText.apply {
                text = error
                visibility = View.VISIBLE
            }
        }
    }

    private fun scrollToCurrentHour(hourlyForecast: List<HourlyForecast>) {
        val currentHour = DateUtils.getCurrentHour()
        val position = hourlyForecast.indexOfFirst {
            it.time.startsWith(currentHour.toString().padStart(2, '0'))
        }
        if (position != -1) {
            binding.includeDailyForecast.recyclerViewHourlyForecast.scrollToPosition(position)
        }
    }

    private fun updateWeatherStats(precipitation: Double, humidity: Int, windSpeed: Double) {
        binding.textPrecipitation.text = "${precipitation.roundToInt()}%"
        binding.textHumidity.text = "$humidity%"
        binding.textWind.text = "${windSpeed.roundToInt()} km/h"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}