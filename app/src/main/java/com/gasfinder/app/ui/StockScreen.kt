package com.gasfinder.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gasfinder.app.R
import com.gasfinder.app.network.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter

private data class BrandStockState(
    val brand: BrandDto,
    var status: String,
    var quantityText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var brandStates by remember { mutableStateOf<List<BrandStockState>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val brandsResponse = RetrofitClient.authApi.getAllBrands()
                val stockResponse = RetrofitClient.authApi.getMyStock()

                if (brandsResponse.isSuccessful) {
                    val brands = brandsResponse.body()?.changes ?: emptyList()
                    val currentStock = if (stockResponse.isSuccessful) {
                        stockResponse.body() ?: emptyList()
                    } else {
                        emptyList()
                    }
                    val stockByBrandId = currentStock.associateBy { it.brandId }

                    brandStates = brands.sortedBy { it.displayOrder }.map { brand ->
                        val existing = stockByBrandId[brand.id]
                        BrandStockState(
                            brand = brand,
                            status = existing?.status?.lowercase() ?: "out",
                            quantityText = existing?.quantity?.toString() ?: ""
                        )
                    }
                } else {
                    errorMessage = context.getString(R.string.stock_error_server, brandsResponse.code())
                }
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.stock_error_generic, e.message)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stock_title)) },
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
            if (savedMessage.isNotEmpty()) {
                Text(savedMessage, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(brandStates) { state ->
                        BrandStockRow(
                            state = state,
                            onStatusChange = { newStatus ->
                                brandStates = brandStates.map {
                                    if (it.brand.id == state.brand.id) it.copy(status = newStatus) else it
                                }
                            },
                            onQuantityChange = { newQty ->
                                brandStates = brandStates.map {
                                    if (it.brand.id == state.brand.id) it.copy(quantityText = newQty) else it
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isSaving = true
                        errorMessage = ""
                        savedMessage = ""
                        scope.launch {
                            try {
                                val nowIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.now().atOffset(java.time.ZoneOffset.UTC))
                                val updates = brandStates.map {
                                    StockUpdateRequest(
                                        brandId = it.brand.id,
                                        status = it.status,
                                        quantity = it.quantityText.toIntOrNull(),
                                        reportedAt = nowIso
                                    )
                                }
                                val response = RetrofitClient.authApi.submitStockUpdates(
                                    StockUpdateBatchRequest(updates)
                                )
                                if (response.isSuccessful) {
                                    savedMessage = context.getString(R.string.stock_saved)
                                } else {
                                    errorMessage = context.getString(R.string.stock_error_server, response.code())
                                }
                            } catch (e: Exception) {
                                errorMessage = context.getString(R.string.stock_error_generic, e.message)
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSaving) stringResource(R.string.stock_save_button_loading) else stringResource(R.string.stock_save_button))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandStockRow(
    state: BrandStockState,
    onStatusChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(state.brand.name, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = state.status == "available",
                    onClick = { onStatusChange("available") },
                    label = {
                        Text(
                            stringResource(R.string.stock_status_available),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.status == "low",
                    onClick = { onStatusChange("low") },
                    label = {
                        Text(
                            stringResource(R.string.stock_status_low),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.status == "out",
                    onClick = { onStatusChange("out") },
                    label = {
                        Text(
                            stringResource(R.string.stock_status_out),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.quantityText,
                onValueChange = onQuantityChange,
                label = { Text(stringResource(R.string.stock_quantity_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
