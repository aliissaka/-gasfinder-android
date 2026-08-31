package com.gasfinder.app.network

data class RetailerListItem(
    val id: String,
    val shopName: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val photoUrl: String?,
    val updatedAt: String,
    val availableBrandIds: List<String>
)

data class StockItemDto(
    val brandId: String,
    val brandName: String,
    val logoUrl: String,
    val status: String,
    val quantity: Int?,
    val lastUpdatedAt: String
)

data class RetailerDetail(
    val id: String,
    val shopName: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val address: String?,
    val photoUrl: String?,
    val openingHours: String,
    val updatedAt: String,
    val stock: List<StockItemDto>
)
