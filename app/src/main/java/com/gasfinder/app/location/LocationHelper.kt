package com.gasfinder.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.*

class LocationHelper(context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onResult: (Location?) -> Unit) {
        var completed = false
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (completed) return
                completed = true
                fusedClient.removeLocationUpdates(this)
                onResult(result.lastLocation)
            }
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).setMaxUpdates(1).build()

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        mainHandler.postDelayed({
            if (!completed) {
                completed = true
                fusedClient.removeLocationUpdates(callback)
                onResult(null)
            }
        }, 15000)
    }
}
