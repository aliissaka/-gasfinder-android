package com.gasfinder.app.network

data class PendingRetailerDto(
    val id: String,
    val shopName: String,
    val phone: String,
    val ownerName: String?,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val createdAt: String
)

data class RetailerStatusUpdateRequest(
    val status: String,
    val reason: String? = null
)
