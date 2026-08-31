package com.gasfinder.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gasfinder.app.location.LocationHelper
import com.gasfinder.app.network.RetailerListItem
import com.gasfinder.app.network.RetrofitClient
import com.gasfinder.app.network.TokenManager
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onLogout: () -> Unit, onRetailerClick: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var retailers by remember { mutableStateOf<List<RetailerListItem>>(emptyList()) }
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLon by remember { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Define search logic as a lambda BEFORE the launcher that uses it
    val searchNearby: () -> Unit = {
        isLoading = true
        errorMessage = ""
        retailers = emptyList()

        LocationHelper(context).getCurrentLocation { location ->
            if (location == null) {
                isLoading = false
                errorMessage = "Could not get location. Make sure GPS is on."
                return@getCurrentLocation
            }

            userLat = location.latitude
            userLon = location.longitude

            scope.launch {
                try {
                    val response = RetrofitClient.authApi.getNearbyRetailers(
                        lat = location.latitude,
                        lon = location.longitude,
                        radiusMeters = 5000,
                        take = 50
                    )
                    if (response.isSuccessful) {
                        retailers = response.body() ?: emptyList()
                        if (retailers.isEmpty()) {
                            errorMessage = "No retailers found within 5km."
                        }
                    } else {
                        errorMessage = "Server error: ${response.code()}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) searchNearby()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Welcome to GasFinder", style = MaterialTheme.typography.headlineSmall)
        Text("Role: ${TokenManager.getRole() ?: "unknown"}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (!hasPermission) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Location Permission")
            }
        } else {
            Button(
                onClick = searchNearby,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Searching..." else "Find Nearby Retailers (5km)")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(retailers) { retailer ->
                RetailerCard(
                    retailer = retailer,
                    userLat = userLat,
                    userLon = userLon,
                    onClick = { onRetailerClick(retailer.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                TokenManager.clear()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

@Composable
fun RetailerCard(
    retailer: RetailerListItem,
    userLat: Double?,
    userLon: Double?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(retailer.shopName, style = MaterialTheme.typography.titleMedium)
            retailer.phone?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))

            val distanceText = if (userLat != null && userLon != null) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLat, userLon,
                    retailer.latitude, retailer.longitude,
                    results
                )
                "%.2f km away".format(results[0] / 1000)
            } else {
                ""
            }

            if (distanceText.isNotEmpty()) {
                Text(distanceText, style = MaterialTheme.typography.bodySmall)
            }

            if (retailer.availableBrandIds.isNotEmpty()) {
                Text(
                    "${retailer.availableBrandIds.size} brand(s) in stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
