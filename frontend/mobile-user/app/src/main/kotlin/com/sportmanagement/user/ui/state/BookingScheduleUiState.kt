package com.sportmanagement.user.ui.state

import com.sportmanagement.user.domain.model.BookingSelectionSummary

data class BookingScheduleUiState(
    val selectedDateText: String = "",
    val showDatePicker: Boolean = false,
    val sliderValue: Float = 46f,
    val selectedSlots: Set<String> = emptySet(),
    val summary: BookingSelectionSummary? = null,
    val showSelectedRange: Boolean = true
)
