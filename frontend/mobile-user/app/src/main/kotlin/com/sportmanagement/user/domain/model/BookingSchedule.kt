package com.sportmanagement.user.domain.model

/**
 * Represents the status of a single time slot on the booking grid.
 */
enum class SlotStatus {
    /** Available for booking */
    AVAILABLE,

    /** Already booked by someone */
    BOOKED,

    /** Locked / unavailable */
    LOCKED,

    /** Reserved for a special event */
    EVENT,

    /** Currently selected by the user */
    SELECTED
}

/**
 * A single cell in the booking grid: one court × one time-slot.
 */
data class TimeSlot(
    val timeLabel: String,
    val status: SlotStatus = SlotStatus.AVAILABLE
)

/**
 * One row in the grid – a court with its time-slots for the day.
 */
data class CourtRow(
    val courtName: String,
    val slots: List<TimeSlot>
)

/**
 * Full schedule data for the booking-schedule screen.
 */
data class BookingScheduleData(
    val selectedDate: String,
    val timeHeaders: List<String>,
    val courts: List<CourtRow>,
    val selectedCourtName: String = "",
    val selectedStartTime: String = "",
    val selectedEndTime: String = "",
    val durationMinutes: Int = 0,
    val selectedSlotCount: Int = 0,
    val estimatedPrice: String = "0đ"
)
