package com.sportmanagement.manager.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BlockedSlotDto(
    @SerializedName("slot_id")    val slotId: Int,
    @SerializedName("field_id")   val fieldId: Int,
    @SerializedName("court_id")   val courtId: Int?,
    @SerializedName("block_date") val blockDate: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time")   val endTime: String,
    val reason: String?,
    @SerializedName("block_type") val blockType: String,
    @SerializedName("court_code") val courtCode: String?,
    @SerializedName("court_name") val courtName: String?
)

data class BlockedSlotsResponse(
    val success: Boolean?,
    val data: List<BlockedSlotDto>?
)

data class CreateBlockedSlotRequest(
    @SerializedName("block_date") val blockDate: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time")   val endTime: String,
    val reason: String? = null,
    @SerializedName("block_type") val blockType: String = "maintenance",
    @SerializedName("court_id")   val courtId: Int? = null
)

data class BookingHistoryDto(
    @SerializedName("history_id")   val historyId: Int,
    @SerializedName("booking_id")   val bookingId: Int,
    val action: String,
    @SerializedName("from_status")  val fromStatus: String?,
    @SerializedName("to_status")    val toStatus: String?,
    val note: String?,
    val author: String?,
    @SerializedName("created_at")   val createdAt: String?
)

data class BookingHistoryResponse(
    val success: Boolean?,
    val data: List<BookingHistoryDto>?
)

data class CreateBookingRequest(
    @SerializedName("field_id")       val fieldId: Int,
    @SerializedName("court_id")       val courtId: Int? = null,
    @SerializedName("customer_phone") val customerPhone: String? = null,
    @SerializedName("start_time")     val startTime: String,
    @SerializedName("end_time")       val endTime: String,
    val note: String? = null,
    val price: Double? = null
)

data class CreateBookingResponse(
    val success: Boolean?,
    val message: String?,
    val data: BookingDto?
)
