package com.sportmanagement.manager.data.remote.dto

data class DashboardStatsDto(
    val totalFields: Int = 0,
    val activeFields: Int = 0,
    val totalBookings: Int = 0,
    val pendingBookings: Int = 0,
    val confirmedBookings: Int = 0,
    val completedBookings: Int = 0,
    val cancelledBookings: Int = 0,
    val rejectedBookings: Int = 0,
    val todayBookings: Int = 0,
    val totalRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val topFieldName: String? = null,
    val topFieldRevenue: Double? = null
)

data class MonthlyRevenueDto(
    val month: Int,
    val revenue: Double,
    val bookings: Int
)
