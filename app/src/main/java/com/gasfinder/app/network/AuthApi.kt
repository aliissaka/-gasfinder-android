package com.gasfinder.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register-retailer")
    suspend fun registerRetailer(@Body request: RegisterRetailerRequest): Response<AuthResponse>
}
