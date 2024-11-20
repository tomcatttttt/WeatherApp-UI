package com.nikita.weatherappui.utils

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient

class LocationManager(
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {
    @SuppressLint("MissingPermission")
    fun fetchLocation(
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onFailure(Exception("Location is null"))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LocationManager", "Failed to fetch location: ${exception.message}")
                onFailure(exception)
            }
    }
}