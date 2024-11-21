package com.nikita.weatherappui.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.gms.location.LocationServices
import com.nikita.weatherappui.R
import com.nikita.weatherappui.databinding.FragmentMainBinding
import com.nikita.weatherappui.model.ForecastDay
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

    private val internetCheckHandler = Handler()
    private var internetCheckRunnable: Runnable = Runnable {}
    private var isFetchingData = false

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchUserLocation()
            } else {
                showError(getString(R.string.permission_denied_for_location_acces))
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        if (isInternetAvailable()) {
            if (isGpsPermissionGranted()) {
                hideGpsMessage()
                showLoadingState()
                setupDependencies()
                setupUI()
                handleNavigationArguments()
            } else {
                handleGpsPermissionDenied()
            }
        } else {
            showNoInternetMessage()
            startInternetCheckLoop()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEnableGpsButton()

        if (isInternetAvailable()) {
            if (isGpsPermissionGranted()) {
                showLoadingState()
                setupDependencies()
                setupUI()
                handleNavigationArguments()
            } else {
                handleGpsPermissionDenied()
            }
        } else {
            showNoInternetMessage()
            startInternetCheckLoop()
        }
    }
    private fun hideGpsMessage() {
        binding.gpsRequiredMessage.visibility = View.GONE
        binding.enableGpsButton.visibility = View.GONE
    }
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun isGpsPermissionGranted(): Boolean {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        return requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun showNoInternetMessage() {
        AnimationHelper.animateVisibility(binding.noInternetMessage, View.VISIBLE)
        AnimationHelper.animateVisibility(binding.noInternetProgressBar, View.VISIBLE)
        AnimationHelper.animateVisibility(binding.mainContent, View.GONE)
    }

    private fun showMainContent() {
        AnimationHelper.animateVisibility(binding.noInternetMessage, View.GONE)
        AnimationHelper.animateVisibility(binding.noInternetProgressBar, View.GONE)
        AnimationHelper.animateVisibility(binding.mainContent, View.VISIBLE)
    }

    private fun showLoadingState() {
        binding.noInternetMessage.visibility = View.GONE
        binding.noInternetProgressBar.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE
    }

    private fun startInternetCheckLoop() {
        internetCheckRunnable = Runnable {
            if (isInternetAvailable()) {
                showLoadingState()
                internetCheckHandler.removeCallbacks(internetCheckRunnable)
                setupDependencies()
                setupUI()
                handleNavigationArguments()
            } else {
                binding.noInternetMessage.visibility = View.VISIBLE
                binding.noInternetProgressBar.visibility = View.VISIBLE
                internetCheckHandler.postDelayed(internetCheckRunnable, 2000)
            }
        }
        internetCheckHandler.post(internetCheckRunnable)
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
        setupLocationClickListener()
    }

    private fun handleNavigationArguments() {
        val sourceScreen = arguments?.getString("sourceScreen") ?: "unknown"
        val locationType = arguments?.getString("locationType") ?: "default"
        val customLocation = arguments?.getString("customLocation")

        when {
            sourceScreen == "search" && locationType == "custom" && !customLocation.isNullOrEmpty() -> {
                fetchWeatherByCustomLocation(customLocation)
            }
            sourceScreen == "search" && locationType == "current" -> {
                fetchUserLocation()
            }
            else -> {
                permissionHandler.checkAndRequestLocationPermission(
                    onPermissionGranted = { fetchUserLocation() },
                    onPermissionDenied = { showError(getString(R.string.permission_denied_for_location_access)) }
                )
            }
        }
    }

    private fun setupLocationClickListener() {
        binding.locationLayout.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToSearchLocationFragment()
            findNavController().navigate(action)
        }
    }

    private fun fetchUserLocation() {
        locationManager.fetchLocation(
            onSuccess = { location ->
                fetchWeather(location.latitude.toString(), location.longitude.toString())
            },
            onFailure = { showError("Failed to fetch user location.") }
        )
    }

    private fun fetchWeather(lat: String, lon: String) {
        isFetchingData = true
        weatherViewModel.fetchTodayWeather(lat, lon)
        weatherViewModel.fetchForecast(lat, lon, days = 7)
    }

    private fun fetchWeatherByCustomLocation(location: String) {
        isFetchingData = true
        weatherViewModel.fetchTodayWeather(location, "")
        weatherViewModel.fetchForecast(location, "", days = 7)
    }

    private fun setupRecyclerView() {
        hourlyForecastAdapter = HourlyForecastAdapter(emptyList(), cityTime) { forecast ->
            updateUIForSelectedHour(forecast)
        }
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

    private fun updateUIForSelectedHour(forecast: HourlyForecast) {
        binding.temperatureText.text = "${forecast.temperature}°"
        binding.precipitationInfo.text = forecast.time
        binding.weatherIcon.load(forecast.iconUrl)
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
            binding.nextForecastButton.text = getString(R.string.next_forecast)
        } else {
            AnimationHelper.animateVisibility(todayForecastContainer, View.GONE)
            AnimationHelper.animateVisibility(nextDaysForecastContainer, View.VISIBLE)
            binding.nextForecastButton.text = getString(R.string.home)
        }

        isNextForecastVisible = !isNextForecastVisible
    }

    private fun setupObservers() {
        weatherViewModel.currentWeather.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                binding.locationText.text = weather.location.name
                binding.temperatureText.text = "${weather.current.tempC.roundToInt()}°"
                binding.precipitationInfo.text = weather.current.condition.text

                cityTime = weather.location.localtime.substringAfter(" ")
                binding.includeDailyForecast.dateText.text = DateUtils.formatDate(weather.location.localtime)

                val forecastToday = weather.forecast.forecastday.firstOrNull()
                forecastToday?.let {
                    binding.maxMinTemp.text =
                        "Max.: ${it.day.maxTempC.roundToInt()}° Min.: ${it.day.minTempC.roundToInt()}°"
                    updateWeatherIcon(it)
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
                if (isFetchingData) {
                    isFetchingData = false
                    showMainContent()
                }
            } else {
                showError(getString(R.string.failed_to_load_today_s_weather_data))
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
                showError(getString(R.string.failed_to_load_weekly_forecast_data))
                navigateBackToSearchWithDelay()
            }
        }

        weatherViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            showError(error)
            navigateBackToSearchWithDelay()
        }
    }
    private fun navigateBackToSearchWithDelay() {
        Handler().postDelayed({
            if (isAdded && findNavController().currentDestination?.id == R.id.mainFragment) {
                val action = MainFragmentDirections.actionMainFragmentToSearchLocationFragment()
                findNavController().navigate(action)
            }
        }, 2000)
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

    private fun updateWeatherIcon(forecastDay: ForecastDay) {
        val currentHour = DateUtils.getCurrentHour()
        val closestHour = forecastDay.hour.minByOrNull {
            val hour = it.time.substringAfter(" ").split(":")[0].toInt()
            Math.abs(hour - currentHour)
        }

        closestHour?.let {
            binding.weatherIcon.load("https:${it.condition.icon}")
        } ?: run {
            binding.weatherIcon.setImageResource(R.drawable.sun_cloud_mid_rain)
        }
    }

    private fun updateWeatherStats(precipitation: Double, humidity: Int, windSpeed: Double) {
        binding.textPrecipitation.text = "${precipitation.roundToInt()}%"
        binding.textHumidity.text = "$humidity%"
        binding.textWind.text = "${windSpeed.roundToInt()} km/h"
    }
    private fun handleGpsPermissionDenied() {
        binding.gpsRequiredMessage.visibility = View.VISIBLE
        binding.enableGpsButton.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE
        binding.noInternetMessage.visibility = View.GONE
        binding.noInternetProgressBar.visibility = View.GONE
    }

    private fun setupEnableGpsButton() {
        binding.enableGpsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        }
    }
    private fun showError(message: String) {
        binding.noInternetMessage.apply {
            text = message
            visibility = View.VISIBLE
        }
        binding.noInternetProgressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        internetCheckHandler.removeCallbacks(internetCheckRunnable)
    }
}