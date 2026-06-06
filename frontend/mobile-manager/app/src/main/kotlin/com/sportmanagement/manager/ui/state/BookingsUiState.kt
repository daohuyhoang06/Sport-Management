package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.BookingCustomer
import com.sportmanagement.manager.domain.model.BookingHistoryEvent
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.domain.model.BookingStatus

data class DayChipData(val dayLabel: String, val dayNumber: String, val isSelected: Boolean)
data class PitchFilterData(val label: String, val isSelected: Boolean)

data class BookingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dayChips: List<DayChipData> = listOf(
        DayChipData("T2", "23", true),
        DayChipData("T3", "24", false),
        DayChipData("T4", "25", false),
        DayChipData("T5", "26", false),
        DayChipData("T6", "27", false)
    ),
    val pitchFilters: List<PitchFilterData> = listOf(
        PitchFilterData("Tất cả", true)
    ),
    val bookings: List<BookingItem> = emptyList(),
    val selectedBooking: BookingItem? = null,
    val showAddBooking: Boolean = false,
    val newBookingCourtCode: String = "A1",
    val newBookingDate: String = "23/10/2023",
    val newBookingStart: String = "17:00",
    val newBookingEnd: String = "18:30",
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
)

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
