package com.sportmanagement.user.domain.model

data class BookingConfirmationRange(
    val courtId: String,
    val courtName: String,
    val startTimeLabel: String,
    val endTimeLabel: String,
    val price: Int
)

data class BookingConfirmationData(
    val selectedDate: String,
    val ranges: List<BookingConfirmationRange>,
    val totalMinutes: Int,
    val totalPrice: Int,
    val fieldId: Int? = null,
    val fieldName: String = "",
    val fieldAddress: String = ""
)
