package com.sportmanagement.manager.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean?,
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    val user: UserDto,
    val token: String,
    val refreshToken: String
)

data class UserDto(
    @SerializedName("person_id") val personId: Int,
    val name: String?,
    val email: String?,
    val phone: String?,
    val username: String,
    val role: String,
    val status: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)
