package com.nikita.weatherappui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nikita.weatherappui.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var isTodayForecastVisible = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.locationLayout.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_searchLocationFragment)
        }

        updateForecastVisibility()

        binding.nextForecastButton.setOnClickListener {
            isTodayForecastVisible = !isTodayForecastVisible
            updateForecastVisibility()
        }
    }

    private fun updateForecastVisibility() {
        if (isTodayForecastVisible) {
            binding.includeDailyForecast.nextDaysForecastContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.includeDailyForecast.nextDaysForecastContainer.visibility = View.GONE
                    binding.includeDailyForecast.todayForecastContainer.visibility = View.VISIBLE
                    binding.includeDailyForecast.todayForecastContainer.alpha = 0f
                    binding.includeDailyForecast.todayForecastContainer.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }
                .start()

            animateButtonTextChange(binding.nextForecastButton, "Next Forecast")
        } else {
            binding.includeDailyForecast.todayForecastContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.includeDailyForecast.todayForecastContainer.visibility = View.GONE
                    binding.includeDailyForecast.nextDaysForecastContainer.visibility = View.VISIBLE
                    binding.includeDailyForecast.nextDaysForecastContainer.alpha = 0f
                    binding.includeDailyForecast.nextDaysForecastContainer.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }
                .start()

            animateButtonTextChange(binding.nextForecastButton, "Home")
        }
    }

    private fun animateButtonTextChange(button: Button, newText: String) {
        button.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                button.text = newText
                button.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}