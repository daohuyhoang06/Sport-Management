package com.sportmanagement.user.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.BookingTimeGridSupport
import com.sportmanagement.user.domain.model.BookingTimeRange
import com.sportmanagement.user.domain.usecase.BuildBookingConfirmationDataUseCase
import com.sportmanagement.user.domain.usecase.BuildBookingSelectionSummaryUseCase
import com.sportmanagement.user.ui.state.BookingScheduleUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

import com.sportmanagement.user.domain.repository.UserRepository
import com.sportmanagement.user.data.repository.UserRepositoryImpl
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class BookingScheduleViewModel(
    private val fieldId: Int,
    private val initialDateText: String,
    private val repository: UserRepository = UserRepositoryImpl(),
    private val buildSummaryUseCase: BuildBookingSelectionSummaryUseCase = BuildBookingSelectionSummaryUseCase(),
    private val buildConfirmationDataUseCase: BuildBookingConfirmationDataUseCase = BuildBookingConfirmationDataUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BookingScheduleUiState(
            selectedDateText = initialDateText,
            summary = null,
            isLoading = true
        )
    )
    val uiState: StateFlow<BookingScheduleUiState> = _uiState

    init {
        loadSchedule(initialDateText)
    }

    private fun loadSchedule(date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val schedule = repository.getFieldGrid(fieldId, date)
            _uiState.update {
                it.copy(
                    scheduleData = schedule,
                    isLoading = false,
                    selectedSlots = emptyList(),
                    summary = null
                )
            }
        }
    }

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
        loadSchedule(dateText)
    }

    fun onSliderChange(value: Float) {
        _uiState.update { it.copy(sliderValue = value) }
    }

    fun onSlotClick(courtId: String, startTime: String, endTime: String) {
        _uiState.update { current ->
            val clickedRange = BookingTimeRange(
                courtId = courtId,
                startTime = startTime,
                endTime = endTime
            )
            val overlappingSelection = current.selectedSlots.firstOrNull { existing ->
                existing.courtId == courtId &&
                    BookingTimeGridSupport.rangeOverlaps(existing, clickedRange)
            }

            val updatedSlots = if (overlappingSelection != null) {
                current.selectedSlots - overlappingSelection
            } else {
                (current.selectedSlots + clickedRange).sortedWith(
                    compareBy(
                        { it.courtId },
                        { BookingTimeGridSupport.parseTimeToMinutes(it.startTime) }
                    )
                )
            }
            val scheduleData = current.scheduleData ?: return@update current
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

    fun buildConfirmationData(): BookingConfirmationData? {
        val scheduleData = _uiState.value.scheduleData ?: return null
        return buildConfirmationDataUseCase(scheduleData, _uiState.value.summary)
    }

    private fun summarySignature(summary: com.sportmanagement.user.domain.model.BookingSelectionSummary?): String {
        return summary?.selectedRanges
            ?.joinToString("|") { "${it.courtName}:${it.startTimeLabel}-${it.endTimeLabel}" }
            ?: ""
    }
}
