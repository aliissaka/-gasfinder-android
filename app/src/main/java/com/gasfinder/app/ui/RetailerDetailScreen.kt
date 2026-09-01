package com.gasfinder.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gasfinder.app.R
import com.gasfinder.app.network.RetailerDetail
import com.gasfinder.app.network.RetrofitClient
import com.gasfinder.app.network.StockItemDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerDetailScreen(retailerId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<RetailerDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(retailerId) {
        scope.launch {
            try {
                val response = RetrofitClient.authApi.getRetailerDetail(retailerId)
                if (response.isSuccessful) {
                    detail = response.body()
                } else {
                    errorMessage = "Erreur : ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur : ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.shopName ?: stringResource(R.string.detail_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage.isNotEmpty() -> {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
                detail != null -> {
                    RetailerDetailContent(detail!!)
                }
            }
        }
    }
}

@Composable
fun RetailerDetailContent(detail: RetailerDetail) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(detail.shopName, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            detail.address?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
            detail.phone?.let {
                Text(stringResource(R.string.detail_phone, it), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
            detail.openingHours?.let {
                Text(stringResource(R.string.detail_hours, it), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text(stringResource(R.string.detail_stock_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (detail.stock.isEmpty()) {
            item {
                Text(stringResource(R.string.detail_no_stock), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(detail.stock) { item ->
                StockItemCard(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StockItemCard(item: StockItemDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.brandName, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))

            val statusColor = when (item.status.lowercase()) {
                "available" -> MaterialTheme.colorScheme.primary
                "low" -> MaterialTheme.colorScheme.tertiary
                "out" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }

            val statusLabel = when (item.status.lowercase()) {
                "available" -> stringResource(R.string.detail_status_available)
                "low" -> stringResource(R.string.detail_status_low)
                "out" -> stringResource(R.string.detail_status_out)
                else -> item.status
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = statusLabel,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall
                )
                item.quantity?.let {
                    Text(
                        stringResource(R.string.detail_quantity_kg, it),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
