package com.sportmanagement.user.domain.model

data class BookingSelectedRangeSummary(
    val courtId: String,
    val courtName: String,
    val startTimeLabel: String,
    val endTimeLabel: String,
    val totalMinutes: Int,
    val totalPrice: Int
)

data class BookingSelectionSummary(
    val selectedRanges: List<BookingSelectedRangeSummary>,
    val totalMinutes: Int,
    val totalPrice: Int
)
