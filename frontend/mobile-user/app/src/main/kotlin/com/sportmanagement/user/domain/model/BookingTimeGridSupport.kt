package com.sportmanagement.user.domain.model

object BookingTimeGridSupport {

    fun generateGridSlotStarts(
        openTime: String,
        closeTime: String,
        gridStepMinutes: Int
    ): List<Int> {
        val step = gridStepMinutes.coerceAtLeast(1)
        val openMinutes = parseTimeToMinutes(openTime)
        val closeMinutes = parseTimeToMinutes(closeTime)
        if (closeMinutes <= openMinutes) return emptyList()

        val starts = mutableListOf<Int>()
        var current = openMinutes
        while (current < closeMinutes) {
            starts += current
            current += step
        }
        return starts
    }

    fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hours * 60 + minutes
    }

    fun formatMinutesAsTime(totalMinutes: Int): String {
        val normalized = totalMinutes.coerceAtLeast(0)
        val hours = normalized / 60
        val minutes = normalized % 60
        return "%02d:%02d".format(hours, minutes)
    }

    fun rangeOverlaps(
        startMinutes: Int,
        endMinutes: Int,
        otherStartMinutes: Int,
        otherEndMinutes: Int
    ): Boolean {
        return startMinutes < otherEndMinutes && endMinutes > otherStartMinutes
    }

    fun rangeOverlaps(first: BookingTimeRange, second: BookingTimeRange): Boolean {
        return rangeOverlaps(
            startMinutes = parseTimeToMinutes(first.startTime),
            endMinutes = parseTimeToMinutes(first.endTime),
            otherStartMinutes = parseTimeToMinutes(second.startTime),
            otherEndMinutes = parseTimeToMinutes(second.endTime)
        )
    }

    fun rangeDurationMinutes(range: BookingTimeRange): Int {
        return (parseTimeToMinutes(range.endTime) - parseTimeToMinutes(range.startTime)).coerceAtLeast(0)
    }
}
