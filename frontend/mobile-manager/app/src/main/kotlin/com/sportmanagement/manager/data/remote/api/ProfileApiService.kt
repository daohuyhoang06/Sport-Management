package com.sportmanagement.manager.data.remote.api

import com.sportmanagement.manager.data.remote.dto.ChangePasswordRequest
import com.sportmanagement.manager.data.remote.dto.GenericResponse
import com.sportmanagement.manager.data.remote.dto.ProfileResponse
import com.sportmanagement.manager.data.remote.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ProfileApiService {
    @GET("api/manager/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("api/manager/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileResponse>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<GenericResponse>
}
