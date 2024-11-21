package com.nikita.weatherappui.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nikita.weatherappui.R
import com.nikita.weatherappui.databinding.FragmentSearchLocationBinding

class SearchLocationFragment : Fragment() {

    private var _binding: FragmentSearchLocationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.checkCurrentLocation.setOnClickListener {
            navigateToMainFragment(
                sourceScreen = "search",
                locationType = "current",
                customLocation = null
            )
        }

        binding.checkSearchLocation.setOnClickListener {
            val customLocation = binding.searchInput.text.toString().trim()
            if (customLocation.isNotEmpty()) {
                navigateToMainFragment(
                    sourceScreen = "search",
                    locationType = "custom",
                    customLocation = customLocation
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enter_a_location_to_search),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToMainFragment(sourceScreen: String, locationType: String, customLocation: String?) {
        val action = SearchLocationFragmentDirections.actionSearchLocationFragmentToMainFragment(
            sourceScreen = sourceScreen,
            locationType = locationType,
            customLocation = customLocation ?: ""
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}