package com.sportmanagement.user.domain.usecase

import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.domain.model.BookingConfirmationRange
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.BookingSelectionSummary

class BuildBookingConfirmationDataUseCase {

    operator fun invoke(
        scheduleData: BookingScheduleData,
        summary: BookingSelectionSummary?
    ): BookingConfirmationData {
        val summaryRanges = summary?.selectedRanges?.map { range ->
            BookingConfirmationRange(
                courtName = range.courtName,
                startTimeLabel = range.startTimeLabel,
                endTimeLabel = range.endTimeLabel,
                price = range.totalPrice
            )
        }.orEmpty()

        val fallbackRange = if (summaryRanges.isEmpty() && scheduleData.selectedCourtName.isNotBlank()) {
            listOf(
                BookingConfirmationRange(
                    courtName = scheduleData.selectedCourtName,
                    startTimeLabel = scheduleData.selectedStartTime,
                    endTimeLabel = scheduleData.selectedEndTime,
                    price = extractDigits(scheduleData.estimatedPrice) ?: 0
                )
            )
        } else {
            emptyList()
        }

        val ranges = if (summaryRanges.isNotEmpty()) summaryRanges else fallbackRange
        val totalPrice = summary?.totalPrice ?: extractDigits(scheduleData.estimatedPrice) ?: 0
        val totalMinutes = summary?.totalMinutes ?: scheduleData.durationMinutes

        return BookingConfirmationData(
            selectedDate = scheduleData.selectedDate,
            ranges = ranges,
            totalMinutes = totalMinutes,
            totalPrice = totalPrice
        )
    }

    private fun extractDigits(value: String): Int? {
        val digits = value.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toIntOrNull()
    }
}
