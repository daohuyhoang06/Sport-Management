package com.sportmanagement.user.domain.model

/**
 * Represents the visual state of a single grid cell.
 */
enum class SlotStatus {
    AVAILABLE,
    BOOKED,
    LOCKED,
    EVENT,
    SELECTED,
    DISABLED
}

/**
 * A sub-court that can be booked independently.
 */
data class BookingSubCourt(
    val id: String,
    val name: String
)

/**
 * One booked or selected time range on a sub-court.
 */
data class BookingTimeRange(
    val courtId: String,
    val startTime: String,
    val endTime: String
)

/**
 * Dynamic grid configuration for the booking screen.
 */
data class BookingTimeGridData(
    val openTime: String = "",
    val closeTime: String = "",
    val gridStepMinutes: Int = 30,
    val minBookingMinutes: Int = 60,
    val courts: List<BookingSubCourt> = emptyList(),
    val bookedSlots: List<BookingTimeRange> = emptyList(),
    val blockedSlots: List<BookingTimeRange> = emptyList()
)

/**
 * Full schedule data for the booking-schedule screen.
 */
data class BookingScheduleData(
    val selectedDate: String,
    val grid: BookingTimeGridData = BookingTimeGridData(),
    val pricePerHour: Int = 0,
    val estimatedPrice: String = "0đ"
)
