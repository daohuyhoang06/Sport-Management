package com.sportmanagement.user.domain.usecase

import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.BookingSelectedRangeSummary
import com.sportmanagement.user.domain.model.BookingSelectionSummary
import com.sportmanagement.user.domain.model.BookingTimeGridSupport
import com.sportmanagement.user.domain.model.BookingTimeRange

class BuildBookingSelectionSummaryUseCase {

    operator fun invoke(
        scheduleData: BookingScheduleData,
        selectedSlots: List<BookingTimeRange>
    ): BookingSelectionSummary? {
        if (selectedSlots.isEmpty()) return null

        val courtNamesById = scheduleData.grid.courts.associate { it.id to it.name }
        val ranges = selectedSlots
            .distinct()
            .sortedWith(
                compareBy(
                    { courtNamesById[it.courtId] ?: it.courtId },
                    { BookingTimeGridSupport.parseTimeToMinutes(it.startTime) }
                )
            )
            .map { range ->
                val totalMinutes = BookingTimeGridSupport.rangeDurationMinutes(range)
                BookingSelectedRangeSummary(
                    courtName = courtNamesById[range.courtId] ?: range.courtId,
                    startTimeLabel = range.startTime,
                    endTimeLabel = range.endTime,
                    totalMinutes = totalMinutes,
                    totalPrice = resolveRangePrice(scheduleData, totalMinutes)
                )
            }
            .filter { it.totalMinutes > 0 }

        if (ranges.isEmpty()) return null

        return BookingSelectionSummary(
            selectedRanges = ranges,
            totalMinutes = ranges.sumOf { it.totalMinutes },
            totalPrice = ranges.sumOf { it.totalPrice }
        )
    }

    private fun resolveRangePrice(scheduleData: BookingScheduleData, totalMinutes: Int): Int {
        if (scheduleData.pricePerHour > 0) {
            return (scheduleData.pricePerHour * totalMinutes) / 60
        }

        val estimatedPrice = extractDigits(scheduleData.estimatedPrice)
        val referenceMinutes = scheduleData.grid.minBookingMinutes.coerceAtLeast(1)
        return if (estimatedPrice != null && estimatedPrice > 0) {
            (estimatedPrice * totalMinutes) / referenceMinutes
        } else {
            DEFAULT_PRICE_PER_HOUR * totalMinutes / 60
        }
    }

    private fun extractDigits(value: String): Int? {
        val digits = value.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toIntOrNull()
    }

    companion object {
        private const val DEFAULT_PRICE_PER_HOUR = 200_000
    }
}
