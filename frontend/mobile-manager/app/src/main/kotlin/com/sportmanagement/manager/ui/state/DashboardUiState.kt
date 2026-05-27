package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.DashboardStats
import com.sportmanagement.manager.domain.model.RevenuePeriod
import com.sportmanagement.manager.domain.model.UpcomingBooking
import com.sportmanagement.manager.domain.model.WeeklyRevenuePoint

data class DashboardUiState(
    val managerName: String = "Quản lý Minh",
    val managerAvatarUrl: String? = null,
    val stats: DashboardStats = DashboardStats(
        revenue = 4_250_000L,
        revenueTrendPercent = 12,
        bookingCount = 18,
        occupancyRate = 85,
        topPitchName = "Sân Mỹ Đình A",
        topPitchRevenue = 18_500_000L
    ),
    val selectedPeriod: RevenuePeriod = RevenuePeriod.WEEK,
    val dailyRevenue: List<WeeklyRevenuePoint> = listOf(
        WeeklyRevenuePoint("6h", 0.10f),
        WeeklyRevenuePoint("8h", 0.30f),
        WeeklyRevenuePoint("10h", 0.45f),
        WeeklyRevenuePoint("12h", 0.25f),
        WeeklyRevenuePoint("14h", 0.55f),
        WeeklyRevenuePoint("16h", 0.80f),
        WeeklyRevenuePoint("18h", 1.00f),
        WeeklyRevenuePoint("20h", 0.90f)
    ),
    val weeklyRevenue: List<WeeklyRevenuePoint> = listOf(
        WeeklyRevenuePoint("T2", 0.38f),
        WeeklyRevenuePoint("T3", 0.50f),
        WeeklyRevenuePoint("T4", 0.60f),
        WeeklyRevenuePoint("T5", 0.52f),
        WeeklyRevenuePoint("T6", 0.72f),
        WeeklyRevenuePoint("T7", 0.90f),
        WeeklyRevenuePoint("CN", 0.84f)
    ),
    val monthlyRevenue: List<WeeklyRevenuePoint> = listOf(
        WeeklyRevenuePoint("T1", 0.45f),
        WeeklyRevenuePoint("T2", 0.52f),
        WeeklyRevenuePoint("T3", 0.48f),
        WeeklyRevenuePoint("T4", 0.65f),
        WeeklyRevenuePoint("T5", 0.70f),
        WeeklyRevenuePoint("T6", 0.60f),
        WeeklyRevenuePoint("T7", 0.78f),
        WeeklyRevenuePoint("T8", 0.85f)
    ),
    val yearlyRevenue: List<WeeklyRevenuePoint> = listOf(
        WeeklyRevenuePoint("T1", 0.40f),
        WeeklyRevenuePoint("T2", 0.45f),
        WeeklyRevenuePoint("T3", 0.55f),
        WeeklyRevenuePoint("T4", 0.60f),
        WeeklyRevenuePoint("T5", 0.72f),
        WeeklyRevenuePoint("T6", 0.68f),
        WeeklyRevenuePoint("T7", 0.80f),
        WeeklyRevenuePoint("T8", 0.85f),
        WeeklyRevenuePoint("T9", 0.78f),
        WeeklyRevenuePoint("T10", 0.90f),
        WeeklyRevenuePoint("T11", 0.88f),
        WeeklyRevenuePoint("T12", 0.95f)
    ),
    val upcomingBooking: UpcomingBooking? = UpcomingBooking(
        courtName = "Sân 7",
        teamName = "FC Anh Em",
        startTime = "17:30",
        endTime = "19:00",
        isPaid = true,
        minutesUntilStart = 15
    )
) {
    val activeRevenue: List<WeeklyRevenuePoint>
        get() = when (selectedPeriod) {
            RevenuePeriod.DAY -> dailyRevenue
            RevenuePeriod.WEEK -> weeklyRevenue
            RevenuePeriod.MONTH -> monthlyRevenue
            RevenuePeriod.YEAR -> yearlyRevenue
        }
}
