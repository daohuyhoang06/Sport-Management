package com.sportmanagement.user.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.usecase.BuildBookingConfirmationDataUseCase
import com.sportmanagement.user.domain.usecase.BuildBookingSelectionSummaryUseCase
import com.sportmanagement.user.ui.state.BookingScheduleUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class BookingScheduleViewModel(
    private val scheduleData: BookingScheduleData,
    private val buildSummaryUseCase: BuildBookingSelectionSummaryUseCase = BuildBookingSelectionSummaryUseCase(),
    private val buildConfirmationDataUseCase: BuildBookingConfirmationDataUseCase = BuildBookingConfirmationDataUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BookingScheduleUiState(
            selectedDateText = scheduleData.selectedDate,
            summary = null
        )
    )
    val uiState: StateFlow<BookingScheduleUiState> = _uiState

    fun onDatePickerVisibilityChange(visible: Boolean) {
        _uiState.update { it.copy(showDatePicker = visible) }
    }

    fun onDatePicked(dateText: String) {
        _uiState.update {
            it.copy(
                selectedDateText = dateText,
                showDatePicker = false
            )
        }
    }

    fun onSliderChange(value: Float) {
        _uiState.update { it.copy(sliderValue = value) }
    }

    fun onToggleSlot(slotKey: String) {
        _uiState.update { current ->
            val updatedSlots = if (current.selectedSlots.contains(slotKey)) {
                current.selectedSlots - slotKey
            } else {
                current.selectedSlots + slotKey
            }
            val nextSummary = buildSummaryUseCase(scheduleData, updatedSlots)
            val shouldResetRangeVisibility = summarySignature(current.summary) != summarySignature(nextSummary)
            current.copy(
                selectedSlots = updatedSlots,
                summary = nextSummary,
                showSelectedRange = if (shouldResetRangeVisibility) true else current.showSelectedRange
            )
        }
    }

    fun onToggleSelectedRangeVisibility() {
        _uiState.update { it.copy(showSelectedRange = !it.showSelectedRange) }
    }

    fun buildConfirmationData(): BookingConfirmationData {
        return buildConfirmationDataUseCase(scheduleData, _uiState.value.summary)
    }

    private fun summarySignature(summary: com.sportmanagement.user.domain.model.BookingSelectionSummary?): String {
        return summary?.selectedRanges
            ?.joinToString("|") { "${it.courtName}:${it.startTimeLabel}-${it.endTimeLabel}" }
            ?: ""
    }
}
