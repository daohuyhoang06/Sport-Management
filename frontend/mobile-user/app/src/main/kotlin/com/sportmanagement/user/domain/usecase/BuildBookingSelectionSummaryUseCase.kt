package com.sportmanagement.user.domain.usecase

import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.BookingSelectedRangeSummary
import com.sportmanagement.user.domain.model.BookingSelectionSummary

class BuildBookingSelectionSummaryUseCase {

    operator fun invoke(
        scheduleData: BookingScheduleData,
        selectedSlots: Set<String>
    ): BookingSelectionSummary? {
        if (selectedSlots.isEmpty()) return null

        val parsedSelections = selectedSlots
            .mapNotNull { parseSlotKey(it) }
        if (parsedSelections.isEmpty()) return null

        val unitPrice = resolveUnitPricePerHalfHour(scheduleData)
        val ranges = mutableListOf<BookingSelectedRangeSummary>()

        parsedSelections
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            .toSortedMap()
            .forEach { (courtIndex, slotIndices) ->
                val court = scheduleData.courts.getOrNull(courtIndex) ?: return@forEach
                val sortedIndices = slotIndices.distinct().sorted()
                splitContiguousRanges(sortedIndices).forEach { range ->
                    val slotCount = range.last - range.first + 1
                    ranges += BookingSelectedRangeSummary(
                        courtName = court.courtName,
                        startTimeLabel = scheduleData.timeHeaders.getOrElse(range.first) { "0:00" },
                        endTimeLabel = formatEndTime(scheduleData.timeHeaders, range.last + 1),
                        totalMinutes = slotCount * 30,
                        totalPrice = slotCount * unitPrice
                    )
                }
            }

        if (ranges.isEmpty()) return null

        return BookingSelectionSummary(
            selectedRanges = ranges,
            totalMinutes = ranges.sumOf { it.totalMinutes },
            totalPrice = ranges.sumOf { it.totalPrice }
        )
    }

    private fun resolveUnitPricePerHalfHour(scheduleData: BookingScheduleData): Int {
        val estimatedPrice = extractDigits(scheduleData.estimatedPrice)
        val baseSlots = scheduleData.selectedSlotCount
        return if (estimatedPrice != null && estimatedPrice > 0 && baseSlots > 0) {
            (estimatedPrice / baseSlots).coerceAtLeast(1)
        } else {
            DEFAULT_PRICE_PER_HALF_HOUR
        }
    }

    private fun formatEndTime(headers: List<String>, endExclusiveIndex: Int): String {
        if (endExclusiveIndex < headers.size) return headers[endExclusiveIndex]
        val baseIndex = endExclusiveIndex - 1
        val baseTime = headers.getOrNull(baseIndex) ?: return "24:00"
        val parts = baseTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return "24:00"
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val plusThirty = hour * 60 + minute + 30
        val endHour = (plusThirty / 60).coerceAtMost(24)
        val endMinute = plusThirty % 60
        return "%d:%02d".format(endHour, endMinute)
    }

    private fun splitContiguousRanges(indices: List<Int>): List<IntRange> {
        if (indices.isEmpty()) return emptyList()
        val ranges = mutableListOf<IntRange>()
        var start = indices.first()
        var end = start

        indices.drop(1).forEach { current ->
            if (current == end + 1) {
                end = current
            } else {
                ranges += start..end
                start = current
                end = current
            }
        }
        ranges += start..end
        return ranges
    }

    private fun parseSlotKey(slotKey: String): Pair<Int, Int>? {
        val split = slotKey.split(":")
        if (split.size != 2) return null
        val courtIndex = split[0].toIntOrNull() ?: return null
        val slotIndex = split[1].toIntOrNull() ?: return null
        return courtIndex to slotIndex
    }

    private fun extractDigits(value: String): Int? {
        val digits = value.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toIntOrNull()
    }

    companion object {
        private const val DEFAULT_PRICE_PER_HALF_HOUR = 100_000
    }
}
