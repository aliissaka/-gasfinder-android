package com.gasfinder.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gasfinder.app.R
import com.gasfinder.app.location.LocationHelper
import com.gasfinder.app.network.RegisterRetailerRequest
import com.gasfinder.app.network.RetrofitClient
import com.gasfinder.app.network.TokenManager
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var shopPhone by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }

    var detectedLat by remember { mutableStateOf<Double?>(null) }
    var detectedLon by remember { mutableStateOf<Double?>(null) }
    var isDetectingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val detectLocation: () -> Unit = {
        isDetectingLocation = true
        locationError = ""
        LocationHelper(context).getCurrentLocation { location: Location? ->
            isDetectingLocation = false
            if (location == null) {
                locationError = context.getString(R.string.register_location_error)
            } else {
                detectedLat = location.latitude
                detectedLon = location.longitude
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) detectLocation()
    }

    LaunchedEffect(Unit) {
        if (hasPermission) detectLocation() else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text(stringResource(R.string.register_owner_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ownerPhone,
            onValueChange = { ownerPhone = it },
            label = { Text(stringResource(R.string.register_owner_phone)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text(stringResource(R.string.register_pin_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text(stringResource(R.string.register_shop_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = shopPhone,
            onValueChange = { shopPhone = it },
            label = { Text(stringResource(R.string.register_shop_phone)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = shopAddress,
            onValueChange = { shopAddress = it },
            label = { Text(stringResource(R.string.register_shop_address)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Location detection block
        when {
            isDetectingLocation -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.register_location_detecting))
                }
            }
            detectedLat != null && detectedLon != null -> {
                Text(stringResource(R.string.register_location_detected, detectedLat!!, detectedLon!!))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = detectLocation) {
                    Text(stringResource(R.string.register_location_retry))
                }
            }
            locationError.isNotEmpty() -> {
                Text(locationError, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = detectLocation) {
                    Text(stringResource(R.string.register_location_retry))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (detectedLat == null || detectedLon == null) {
                    statusMessage = context.getString(R.string.register_error_no_location)
                    return@Button
                }
                isSubmitting = true
                statusMessage = ""
                scope.launch {
                    try {
                        val response = RetrofitClient.authApi.registerRetailer(
                            RegisterRetailerRequest(
                                ownerPhone = ownerPhone,
                                ownerName = ownerName,
                                pin = pin,
                                shopName = shopName,
                                shopPhone = shopPhone,
                                shopAddress = shopAddress.ifBlank { null },
                                shopLatitude = detectedLat!!,
                                shopLongitude = detectedLon!!
                            )
                        )
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                TokenManager.saveAuth(body.accessToken, body.role, body.retailerId)
                                onRegisterSuccess()
                            }
                        } else {
                            statusMessage = context.getString(R.string.register_error_failed, response.code())
                        }
                    } catch (e: Exception) {
                        statusMessage = context.getString(R.string.register_error_generic, e.message)
                    } finally {
                        isSubmitting = false
                    }
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSubmitting) stringResource(R.string.register_button_loading) else stringResource(R.string.register_button))
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (statusMessage.isNotEmpty()) {
            Text(statusMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
