package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.data.remote.dto.FieldCourtDto
import com.sportmanagement.manager.data.remote.dto.FieldDto
import com.sportmanagement.manager.domain.model.BookingCustomer
import com.sportmanagement.manager.domain.model.BookingHistoryEvent
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.domain.model.BookingStatus
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayChipData(val dayLabel: String, val dayNumber: String, val isSelected: Boolean, val isoDate: String = "", val isToday: Boolean = false)
data class PitchFilterData(val label: String, val isSelected: Boolean)

private val VI_DAY_LABELS = mapOf(
    Calendar.MONDAY to "T2",
    Calendar.TUESDAY to "T3",
    Calendar.WEDNESDAY to "T4",
    Calendar.THURSDAY to "T5",
    Calendar.FRIDAY to "T6",
    Calendar.SATURDAY to "T7",
    Calendar.SUNDAY to "CN"
)

private val isoDateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val weekLabelFmt = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))

private fun parseIsoDate(raw: String): Calendar? {
    val date = runCatching { isoDateFmt.parse(raw) }.getOrNull() ?: return null
    return Calendar.getInstance().apply { time = date }
}

private fun currentIsoDate(): String = isoDateFmt.format(Date())

private fun startOfWeekIso(dateIso: String): String {
    val calendar = parseIsoDate(dateIso) ?: Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.firstDayOfWeek = Calendar.MONDAY
    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        calendar.add(Calendar.DAY_OF_MONTH, -1)
    }
    return isoDateFmt.format(calendar.time)
}

fun formatWeekRangeLabel(weekStartDateIso: String): String {
    val start = parseIsoDate(weekStartDateIso) ?: return "Tuần hiện tại"
    val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) }
    return "${weekLabelFmt.format(start.time)} - ${weekLabelFmt.format(end.time)}"
}

fun buildWeekDayChips(weekStartDateIso: String, selectedDateIso: String? = null): List<DayChipData> {
    val todayIso = currentIsoDate()
    val weekStart = (parseIsoDate(weekStartDateIso) ?: Calendar.getInstance()).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return (0..6).map { offset ->
        val c = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
        val dow = c.get(Calendar.DAY_OF_WEEK)
        val isoDate = isoDateFmt.format(c.time)
        DayChipData(
            dayLabel = VI_DAY_LABELS[dow] ?: "?",
            dayNumber = c.get(Calendar.DAY_OF_MONTH).toString(),
            isSelected = isoDate == selectedDateIso,
            isoDate = isoDate,
            isToday = isoDate == todayIso
        )
    }
}

fun currentWeekStartIso(): String = startOfWeekIso(currentIsoDate())

data class BookingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val visibleWeekStartDate: String = currentWeekStartIso(),
    val dayChips: List<DayChipData> = buildWeekDayChips(currentWeekStartIso(), currentIsoDate()),
    val selectedDate: String = currentIsoDate(),
    val pitchFilters: List<PitchFilterData> = listOf(
        PitchFilterData("Tất cả", true)
    ),
    val bookings: List<BookingItem> = emptyList(),
    val selectedBooking: BookingItem? = null,
    val showAddBooking: Boolean = false,
    // Fields & courts for new booking form
    val newBookingFields: List<FieldDto> = emptyList(),
    val newBookingSelectedFieldId: Int? = null,
    val newBookingCourts: List<FieldCourtDto> = emptyList(),
    val newBookingCourtId: Int? = null,
    val newBookingCourtCode: String = "",
    val newBookingDate: String = "",
    val newBookingStart: String = "",
    val newBookingEnd: String = "",
    // Booked ranges for availability grid: (startMinutes, endMinutes) since midnight
    val newBookingBookedRanges: List<Pair<Int, Int>> = emptyList(),
    val newBookingIsLoadingSlots: Boolean = false,
    val newBookingCustomerName: String = "",
    val newBookingCustomerPhone: String = "",
    val newBookingDeposit: String = "",
    val newBookingNotes: String = "",

    // Cancel dialog state
    val showCancelDialog: Boolean = false,
    val cancelTargetId: String = "",
    val cancelReasonDraft: String = "",

    // Edit/reschedule dialog state
    val showEditDialog: Boolean = false,
    val editTargetId: String = "",
    val editDate: String = "",
    val editStart: String = "",
    val editEnd: String = "",
    val editCourtCode: String = "",
    val editCustomerName: String = "",
    val editCustomerPhone: String = "",

    // Manual payment dialog state
    val showPaymentDialog: Boolean = false,
    val paymentTargetId: String = "",
    val paymentMethodDraft: String = "Tiền mặt",
    val paymentNoteDraft: String = "",

    // Booking history (per booking id)
    val bookingHistory: Map<String, List<BookingHistoryEvent>> = emptyMap()
) {
    val filteredBookings: List<BookingItem>
        get() {
            val selectedPitch = pitchFilters.firstOrNull { it.isSelected && it.label != "Tất cả" }
            return bookings.filter { booking ->
                val matchPitch = selectedPitch == null ||
                    booking.pitchName.contains(selectedPitch.label, ignoreCase = true) ||
                    booking.courtCode == selectedPitch.label ||
                    booking.courtName.contains(selectedPitch.label, ignoreCase = true)
                matchPitch
            }
        }
}

// Demo data kept for @Preview only — không dùng làm default
private val customerAn = BookingCustomer(
    id = "c1",
    name = "Nguyễn Văn An",
    phone = "090 123 4567",
    email = "nguyenvan.an@gmail.com",
    avatarUrl = null,
    totalBookings = 47,
    totalSpend = 21_150_000L,
    memberSince = "01/2023",
    isVip = true
)

private val customerBich = BookingCustomer(
    id = "c2",
    name = "Trần Thị Bích",
    phone = "091 999 8888",
    email = "tranthi.bich@gmail.com",
    avatarUrl = null,
    totalBookings = 12,
    totalSpend = 5_400_000L,
    memberSince = "06/2023"
)

private val customerNam = BookingCustomer(
    id = "c3",
    name = "Lê Hoàng Nam",
    phone = "098 765 4321",
    email = "lehoang.nam@gmail.com",
    avatarUrl = null,
    totalBookings = 28,
    totalSpend = 12_600_000L,
    memberSince = "03/2023"
)

fun demoBookings() = listOf(
    BookingItem(
        id = "b1",
        pitchName = "Sân Thể Thao ABC",
        courtCode = "A1",
        courtName = "Sân 5 người",
        customer = customerAn,
        date = "23/10/2023",
        dayOfWeek = "Thứ Hai",
        startTime = "17:00",
        endTime = "18:30",
        durationMinutes = 90,
        pricePerHour = 300_000L,
        totalPrice = 450_000L,
        depositPaid = 100_000L,
        status = BookingStatus.CONFIRMED,
        isPaid = true,
        paymentMethod = "Chuyển khoản",
        showLive = true
    ),
    BookingItem(
        id = "b2",
        pitchName = "Sân Thể Thao ABC",
        courtCode = "A1",
        courtName = "Sân 5 người",
        customer = customerBich,
        date = "23/10/2023",
        dayOfWeek = "Thứ Hai",
        startTime = "18:30",
        endTime = "20:00",
        durationMinutes = 90,
        pricePerHour = 300_000L,
        totalPrice = 450_000L,
        depositPaid = 0L,
        status = BookingStatus.PENDING,
        isPaid = false
    ),
    BookingItem(
        id = "b3",
        pitchName = "Sân Thể Thao ABC",
        courtCode = "A1",
        courtName = "Sân 5 người",
        customer = customerNam,
        date = "23/10/2023",
        dayOfWeek = "Thứ Hai",
        startTime = "15:30",
        endTime = "17:00",
        durationMinutes = 90,
        pricePerHour = 300_000L,
        totalPrice = 450_000L,
        depositPaid = 450_000L,
        status = BookingStatus.COMPLETED,
        isPaid = true,
        paymentMethod = "Tiền mặt"
    )
)
