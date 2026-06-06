package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.DashboardStats
import com.sportmanagement.manager.domain.model.RevenuePeriod
import com.sportmanagement.manager.domain.model.UpcomingBooking
import com.sportmanagement.manager.domain.model.WeeklyRevenuePoint

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val managerName: String = "",
    val managerAvatarUrl: String? = null,
    val stats: DashboardStats = DashboardStats(
        revenue = 0L,
        revenueTrendPercent = 0,
        bookingCount = 0,
        occupancyRate = 0,
        topPitchName = "",
        topPitchRevenue = 0L
    ),
    val selectedPeriod: RevenuePeriod = RevenuePeriod.MONTH,
    val dailyRevenue: List<WeeklyRevenuePoint> = emptyList(),
    val weeklyRevenue: List<WeeklyRevenuePoint> = emptyList(),
    val monthlyRevenue: List<WeeklyRevenuePoint> = emptyList(),
    val yearlyRevenue: List<WeeklyRevenuePoint> = emptyList(),
    val upcomingBooking: UpcomingBooking? = null
) {
    val activeRevenue: List<WeeklyRevenuePoint>
        get() = when (selectedPeriod) {
            RevenuePeriod.DAY   -> dailyRevenue
            RevenuePeriod.WEEK  -> weeklyRevenue
            RevenuePeriod.MONTH -> monthlyRevenue
            RevenuePeriod.YEAR  -> yearlyRevenue
        }
}
