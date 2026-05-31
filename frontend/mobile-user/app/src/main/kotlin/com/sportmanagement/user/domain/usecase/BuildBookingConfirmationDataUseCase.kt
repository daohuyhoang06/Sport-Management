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
        val ranges = summary?.selectedRanges?.map { range ->
            BookingConfirmationRange(
                courtName = range.courtName,
                startTimeLabel = range.startTimeLabel,
                endTimeLabel = range.endTimeLabel,
                price = range.totalPrice
            )
        }.orEmpty()
        val totalPrice = summary?.totalPrice ?: 0
        val totalMinutes = summary?.totalMinutes ?: 0

        return BookingConfirmationData(
            selectedDate = scheduleData.selectedDate,
            ranges = ranges,
            totalMinutes = totalMinutes,
            totalPrice = totalPrice
        )
    }
}
