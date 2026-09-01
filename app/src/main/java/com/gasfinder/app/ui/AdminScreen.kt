package com.gasfinder.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gasfinder.app.R
import com.gasfinder.app.network.PendingRetailerDto
import com.gasfinder.app.network.RetailerStatusUpdateRequest
import com.gasfinder.app.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var retailers by remember { mutableStateOf<List<PendingRetailerDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    val loadPending: () -> Unit = {
        isLoading = true
        errorMessage = ""
        scope.launch {
            try {
                val response = RetrofitClient.authApi.listPendingRetailers()
                if (response.isSuccessful) {
                    retailers = response.body() ?: emptyList()
                } else {
                    errorMessage = context.getString(R.string.admin_error_server, response.code())
                }
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.admin_error_generic, e.message)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadPending() }

    val updateStatus: (String, String) -> Unit = { retailerId, newStatus ->
        scope.launch {
            try {
                val response = RetrofitClient.authApi.setRetailerStatus(
                    retailerId,
                    RetailerStatusUpdateRequest(status = newStatus)
                )
                if (response.isSuccessful) {
                    statusMessage = context.getString(R.string.admin_status_updated)
                    loadPending()
                } else {
                    errorMessage = context.getString(R.string.admin_error_server, response.code())
                }
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.admin_error_generic, e.message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
            Text(stringResource(R.string.admin_title), style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (statusMessage.isNotEmpty()) {
            Text(statusMessage, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            retailers.isEmpty() -> {
                Text(stringResource(R.string.admin_empty))
            }
            else -> {
                LazyColumn {
                    items(retailers) { retailer ->
                        PendingRetailerCard(
                            retailer = retailer,
                            onApprove = { updateStatus(retailer.id, "Approved") },
                            onReject = { updateStatus(retailer.id, "Suspended") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PendingRetailerCard(
    retailer: PendingRetailerDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(retailer.shopName, style = MaterialTheme.typography.titleMedium)
            Text(retailer.phone, style = MaterialTheme.typography.bodySmall)
            retailer.ownerName?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            retailer.address?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove) {
                    Text(stringResource(R.string.admin_approve))
                }
                OutlinedButton(onClick = onReject) {
                    Text(stringResource(R.string.admin_reject))
                }
            }
        }
    }
}
