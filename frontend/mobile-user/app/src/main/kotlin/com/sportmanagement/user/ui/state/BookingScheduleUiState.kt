package com.sportmanagement.user.ui.state

import com.sportmanagement.user.domain.model.BookingTimeRange
import com.sportmanagement.user.domain.model.BookingSelectionSummary
import com.sportmanagement.user.domain.model.BookingScheduleData

data class BookingScheduleUiState(
    val selectedDateText: String = "",
    val showDatePicker: Boolean = false,
    val sliderValue: Float = 46f,
    val selectedSlots: List<BookingTimeRange> = emptyList(),
    val summary: BookingSelectionSummary? = null,
    val showSelectedRange: Boolean = true,
    val scheduleData: BookingScheduleData? = null,
    val isLoading: Boolean = true
)
