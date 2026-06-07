package com.sportmanagement.manager.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FieldDto(
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("field_name") val fieldName: String,
    val location: String?,
    val status: String,
    @SerializedName("manager_id") val managerId: Int?,
    val phone: String?,
    @SerializedName("open_time") val openTime: String?,
    @SerializedName("close_time") val closeTime: String?,
    @SerializedName("slot_minutes") val slotMinutes: Int?,
    @SerializedName("slot_price") val slotPrice: Double?,
    @SerializedName("avatar_image_url") val avatarImageUrl: String?,
    @SerializedName("card_image_url") val cardImageUrl: String?,
    @SerializedName("sport_id") val sportId: Int?,
    @SerializedName("sport_name") val sportName: String?
)

// GET /api/manager/fields → {success, data: [...]}
data class FieldListResponse(
    val success: Boolean?,
    val data: List<FieldDto>?
)

// GET/POST/PUT /api/manager/fields/:id → {success, message, data: FieldDto}
data class FieldResponse(
    val success: Boolean?,
    val message: String?,
    val data: FieldDto?
)

data class CreateFieldRequest(
    @SerializedName("field_name") val fieldName: String,
    val location: String,
    @SerializedName("sport_id") val sportId: Int,
    val phone: String? = null,
    @SerializedName("open_time") val openTime: String? = null,
    @SerializedName("close_time") val closeTime: String? = null,
    @SerializedName("slot_price") val slotPrice: Double? = null,
    @SerializedName("slot_minutes") val slotMinutes: Int? = null,
    val status: String = "active"
)

data class UpdateFieldStatusRequest(val status: String)

// Courts
data class FieldCourtDto(
    @SerializedName("court_id") val courtId: Int,
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("court_code") val courtCode: String,
    @SerializedName("court_name") val courtName: String,
    val status: String,
    @SerializedName("sort_order") val sortOrder: Int
)

data class CourtsResponse(
    val success: Boolean?,
    val data: List<FieldCourtDto>?
)

data class CreateCourtRequest(
    @SerializedName("court_code") val courtCode: String,
    @SerializedName("court_name") val courtName: String,
    val status: String = "active"
)

// Services
data class FieldServiceDto(
    val id: Int,
    @SerializedName("field_id") val fieldId: Int,
    @SerializedName("service_name") val serviceName: String,
    val description: String?,
    @SerializedName("is_free") val isFree: Boolean,
    val price: Double
)

data class ServicesResponse(
    val success: Boolean?,
    val data: List<FieldServiceDto>?
)

// Policies
data class FieldPolicyDto(
    val id: Int,
    @SerializedName("field_id") val fieldId: Int,
    val title: String,
    val content: String,
    @SerializedName("policy_type") val policyType: String
)

data class PoliciesResponse(
    val success: Boolean?,
    val data: List<FieldPolicyDto>?
)

data class ActionResponse(
    val success: Boolean?,
    val message: String?
)
