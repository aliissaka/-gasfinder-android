package com.gasfinder.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register-retailer")
    suspend fun registerRetailer(@Body request: RegisterRetailerRequest): Response<LoginResponse>

    @GET("api/retailers")
    suspend fun getNearbyRetailers(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radiusMeters") radiusMeters: Int = 5000,
        @Query("take") take: Int = 50
    ): Response<List<RetailerListItem>>

    @GET("api/retailers/{id}")
    suspend fun getRetailerDetail(@Path("id") id: String): Response<RetailerDetail>

    @GET("api/admin/retailers")
    suspend fun listPendingRetailers(@Query("status") status: String? = "Pending"): Response<List<PendingRetailerDto>>

    @PATCH("api/admin/retailers/{id}/status")
    suspend fun setRetailerStatus(
        @Path("id") id: String,
        @Body request: RetailerStatusUpdateRequest
    ): Response<Unit>
}
