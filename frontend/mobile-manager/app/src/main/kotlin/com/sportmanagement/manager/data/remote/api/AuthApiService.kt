package com.sportmanagement.manager.data.remote.api

import com.sportmanagement.manager.data.remote.dto.LoginRequest
import com.sportmanagement.manager.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
