package com.gasfinder.app.network

import java.util.UUID

data class BrandDto(
    val id: String,
    val name: String,
    val logoUrl: String,
    val displayOrder: Int,
    val updatedAt: String
)

data class BrandSyncResponse(
    val cursor: String,
    val changes: List<BrandDto>,
    val deletes: List<String>
)

data class StockUpdateRequest(
    val clientOutboxId: String = UUID.randomUUID().toString(),
    val brandId: String,
    val bottleSize: String,
    val status: String,
    val quantity: Int?,
    val reportedAt: String
)

data class StockUpdateBatchRequest(
    val updates: List<StockUpdateRequest>
)

data class StockUpdateResult(
    val clientOutboxId: String,
    val outcome: String,
    val message: String?
)

data class StockUpdateBatchResponse(
    val results: List<StockUpdateResult>
)
