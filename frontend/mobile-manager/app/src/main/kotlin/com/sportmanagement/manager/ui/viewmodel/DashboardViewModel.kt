package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.domain.model.DashboardStats
import com.sportmanagement.manager.domain.model.RevenuePeriod
import com.sportmanagement.manager.domain.model.WeeklyRevenuePoint
import com.sportmanagement.manager.ui.state.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        val savedName = AppContainer.authRepository.getManagerName()
        if (!savedName.isNullOrBlank()) {
            _uiState.update { it.copy(managerName = savedName, managerAvatarUrl = AppContainer.authRepository.getAvatarUrl()) }
        }
        loadStats()
        loadRevenueTrend(RevenuePeriod.MONTH)
    }

    fun onPeriodSelected(period: RevenuePeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        val state = _uiState.value
        val isEmpty = when (period) {
            RevenuePeriod.DAY   -> state.dailyRevenue.isEmpty()
            RevenuePeriod.WEEK  -> state.weeklyRevenue.isEmpty()
            RevenuePeriod.MONTH -> state.monthlyRevenue.isEmpty()
            RevenuePeriod.YEAR  -> state.yearlyRevenue.isEmpty()
        }
        if (isEmpty) loadRevenueTrend(period)
    }

    fun refresh() {
        loadStats()
        // Force-clear current period so chart always reloads fresh data
        val period = _uiState.value.selectedPeriod
        _uiState.update { state ->
            when (period) {
                RevenuePeriod.DAY   -> state.copy(dailyRevenue  = emptyList())
                RevenuePeriod.WEEK  -> state.copy(weeklyRevenue = emptyList())
                RevenuePeriod.MONTH -> state.copy(monthlyRevenue = emptyList())
                RevenuePeriod.YEAR  -> state.copy(yearlyRevenue  = emptyList())
            }
        }
        loadRevenueTrend(period)
    }

    fun loadStats(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) _uiState.update { it.copy(isLoading = true, error = null) }
            AppContainer.dashboardRepository.getStats()
                .onSuccess { dto ->
                    val trendPercent = when {
                        dto.yesterdayRevenue > 0 ->
                            ((dto.todayRevenue - dto.yesterdayRevenue) / dto.yesterdayRevenue * 100).toInt()
                        dto.todayRevenue > 0 -> 100
                        else -> 0
                    }
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            stats = DashboardStats(
                                revenue = dto.todayRevenue.toLong(),
                                revenueTrendPercent = trendPercent,
                                bookingCount = dto.todayBookings,
                                occupancyRate = dto.todayOccupancyPercent,
                                topPitchName = dto.topFieldName
                                    ?: if (dto.activeFields > 0) "${dto.activeFields} sân đang hoạt động" else "Chưa có sân",
                                topPitchRevenue = dto.topFieldRevenue?.toLong() ?: dto.totalRevenue.toLong()
                            )
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun loadRevenueTrend(period: RevenuePeriod) {
        viewModelScope.launch {
            val periodKey = when (period) {
                RevenuePeriod.DAY   -> "day"
                RevenuePeriod.WEEK  -> "week"
                RevenuePeriod.MONTH -> "month"
                RevenuePeriod.YEAR  -> "year"
            }
            AppContainer.dashboardRepository.getRevenueTrend(periodKey)
                .onSuccess { list ->
                    if (list.isEmpty()) return@onSuccess
                    val maxRevenue = list.maxOf { it.revenue }.takeIf { it > 0 } ?: 1.0
                    val points = list.map { dto ->
                        WeeklyRevenuePoint(dto.label, (dto.revenue / maxRevenue).toFloat())
                    }
                    _uiState.update { state ->
                        when (period) {
                            RevenuePeriod.DAY   -> state.copy(dailyRevenue = points)
                            RevenuePeriod.WEEK  -> state.copy(weeklyRevenue = points)
                            RevenuePeriod.MONTH -> state.copy(monthlyRevenue = points)
                            RevenuePeriod.YEAR  -> state.copy(yearlyRevenue = points)
                        }
                    }
                }
        }
    }
}
