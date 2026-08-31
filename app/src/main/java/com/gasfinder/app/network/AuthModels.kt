package com.gasfinder.app.network

data class LoginRequest(
    val phone: String,
    val pin: String
)

data class LoginResponse(
    val accessToken: String,
    val role: String,
    val retailerId: String?
)

data class RegisterRetailerRequest(
    val ownerPhone: String,
    val ownerName: String,
    val pin: String,
    val shopName: String,
    val shopPhone: String,
    val shopAddress: String?,
    val shopLatitude: Double,
    val shopLongitude: Double
)
