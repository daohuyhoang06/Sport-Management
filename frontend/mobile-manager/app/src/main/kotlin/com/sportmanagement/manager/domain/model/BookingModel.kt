package com.sportmanagement.manager.domain.model

enum class BookingStatus(val label: String, val badgeLabel: String) {
    PENDING("Chờ xác nhận", "CHỜ DUYỆT"),
    CONFIRMED("Đã xác nhận", "ĐÃ XÁC NHẬN"),
    COMPLETED("Hoàn thành", "ĐÃ XONG"),
    CANCELLED("Đã hủy", "ĐÃ HỦY")
}

data class BookingCustomer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val avatarUrl: String?,
    val totalBookings: Int,
    val totalSpend: Long,
    val memberSince: String,
    val isVip: Boolean = false
)

data class BookingItem(
    val id: String,
    val pitchName: String,
    val courtCode: String,
    val courtName: String,
    val customer: BookingCustomer,
    val date: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val pricePerHour: Long,
    val totalPrice: Long,
    val depositPaid: Long = 0L,
    val status: BookingStatus,
    val isPaid: Boolean,
    val paymentMethod: String = "Tiền mặt",
    val notes: String = "",
    val showLive: Boolean = false,
    val cancelReason: String = "",
    val isManagerCreated: Boolean = false
)

data class BookingHistoryEvent(
    val timestamp: String,
    val action: String,
    val note: String,
    val author: String
)

val BookingItem.historyEvents: List<BookingHistoryEvent>
    get() = when (status) {
        BookingStatus.PENDING -> listOf(
            BookingHistoryEvent("10:30", "Đặt sân", "Khách hàng đặt qua ứng dụng", customer.name)
        )
        BookingStatus.CONFIRMED -> listOf(
            BookingHistoryEvent("10:30", "Đặt sân", "Khách hàng đặt qua ứng dụng", customer.name),
            BookingHistoryEvent("10:35", "Xác nhận", "Quản lý xác nhận lịch đặt", "Quản lý Minh")
        )
        BookingStatus.COMPLETED -> listOf(
            BookingHistoryEvent("10:30", "Đặt sân", "Khách hàng đặt qua ứng dụng", customer.name),
            BookingHistoryEvent("10:35", "Xác nhận", "Quản lý xác nhận lịch đặt", "Quản lý Minh"),
            BookingHistoryEvent("17:00", "Bắt đầu", "Khách hàng vào sân", "Hệ thống"),
            BookingHistoryEvent("18:30", "Hoàn thành", "Kết thúc ca đặt sân", "Hệ thống")
        )
        BookingStatus.CANCELLED -> listOf(
            BookingHistoryEvent("10:30", "Đặt sân", "Khách hàng đặt qua ứng dụng", customer.name),
            BookingHistoryEvent("11:00", "Hủy", "Khách hàng hủy lịch", customer.name)
        )
    }
