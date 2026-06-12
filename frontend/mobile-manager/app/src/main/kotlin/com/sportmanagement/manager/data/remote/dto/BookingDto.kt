package com.sportmanagement.manager.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookingDto(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("field_id") val fieldId: Int?,
    @SerializedName("customer_id") val customerId: Int?,
    @SerializedName("court_id") val courtId: Int?,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    val status: String?,
    val price: Double?,
    val note: String?,
    @SerializedName("field_name") val fieldName: String?,
    val location: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("customer_email") val customerEmail: String?,
    @SerializedName("customer_phone") val customerPhone: String?,
    @SerializedName("court_code") val courtCode: String?,
    @SerializedName("court_name") val courtName: String?,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("payment_status") val paymentStatus: String? = null,
    @SerializedName("payment_amount") val paymentAmount: Double? = null,
    @SerializedName("manager_created") val managerCreated: Int? = null
)

data class BookingActionResponse(
    val message: String?,
    val success: Boolean?
)

data class BookingRejectRequest(val reason: String? = null)
data class BookingCancelRequest(val reason: String? = null)
data class BookingRescheduleRequest(
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String
)
data class BookingRescheduleResponse(
    val success: Boolean?,
    val data: BookingDto?,
    val message: String? = null
)
