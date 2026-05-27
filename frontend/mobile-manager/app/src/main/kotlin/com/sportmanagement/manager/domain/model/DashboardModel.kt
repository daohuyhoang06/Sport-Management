package com.sportmanagement.manager.domain.model

data class DashboardStats(
    val revenue: Long,
    val revenueTrendPercent: Int,
    val bookingCount: Int,
    val occupancyRate: Int,
    val topPitchName: String = "Sân Mỹ Đình A",
    val topPitchRevenue: Long = 0L
)

data class WeeklyRevenuePoint(
    val dayLabel: String,
    val normalizedValue: Float
)

data class UpcomingBooking(
    val courtName: String,
    val teamName: String,
    val startTime: String,
    val endTime: String,
    val isPaid: Boolean,
    val minutesUntilStart: Int
)

enum class RevenuePeriod(val label: String) {
    DAY("Hôm nay"),
    WEEK("Tuần này"),
    MONTH("Tháng này"),
    YEAR("Năm nay")
}
