package com.sportmanagement.manager.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName("person_id")  val personId: Int = 0,
    @SerializedName("person_name") val personName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class ProfileResponse(
    val success: Boolean,
    val data: ProfileDto? = null,
    val message: String? = null
)

data class UpdateProfileRequest(
    @SerializedName("person_name") val personName: String?,
    val email: String?,
    val phone: String?
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class GenericResponse(
    val success: Boolean,
    val message: String? = null
)
