package com.nikita.weatherappui.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nikita.weatherappui.R
import com.nikita.weatherappui.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        if (isAdded && findNavController().currentDestination?.id == R.id.splashFragment) {
            navigateToMainFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delayNavigation()
    }

    private fun delayNavigation() {
        handler.postDelayed(navigateRunnable, 3000)
    }

    private fun navigateToMainFragment() {
        val action = SplashFragmentDirections.actionSplashFragmentToMainFragment(
            sourceScreen = "splash",
            locationType = "default",
            customLocation = null
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(navigateRunnable)
        _binding = null
    }
}