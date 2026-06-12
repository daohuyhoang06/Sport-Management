package com.sportmanagement.user.domain.model

object BookingTimeGridSupport {

    fun generateGridSlotStarts(
        openTime: String,
        closeTime: String,
        gridStepMinutes: Int
    ): List<Int> {
        val step = gridStepMinutes.coerceAtLeast(1)
        val openMinutes = roundMinutesUpToHalfHour(parseTimeToMinutes(openTime))
        val closeMinutes = normalizeCloseMinutes(
            openMinutes = openMinutes,
            closeMinutes = parseTimeToMinutes(closeTime),
            stepMinutes = step
        )
        if (closeMinutes <= openMinutes) return emptyList()

        val starts = mutableListOf<Int>()
        var current = openMinutes
        while (current < closeMinutes) {
            starts += current
            current += step
        }
        return starts
    }

    fun roundMinutesUpToHalfHour(totalMinutes: Int): Int {
        val minutes = totalMinutes.coerceAtLeast(0)
        val remainder = minutes % 30
        return if (remainder == 0) {
            minutes
        } else {
            minutes + (30 - remainder)
        }
    }

    fun roundMinutesToNearestStep(totalMinutes: Int, stepMinutes: Int): Int {
        val step = stepMinutes.coerceAtLeast(1)
        val minutes = totalMinutes.coerceAtLeast(0)
        val remainder = minutes % step
        return when {
            remainder == 0 -> minutes
            remainder * 2 < step -> minutes - remainder
            else -> minutes + (step - remainder)
        }
    }

    fun normalizeCloseMinutes(
        openMinutes: Int,
        closeMinutes: Int,
        stepMinutes: Int
    ): Int {
        if (closeMinutes <= openMinutes) {
            return 24 * 60
        }
        return roundMinutesToNearestStep(closeMinutes, stepMinutes).coerceAtMost(24 * 60)
    }

    fun parseTimeToMinutes(time: String): Int {
        val parts = time.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hours * 60 + minutes
    }

    fun formatMinutesAsTime(totalMinutes: Int): String {
        val normalized = totalMinutes.coerceAtLeast(0)
        val hours = (normalized / 60) % 24
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
