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
        // Lấy tên manager từ session (không cần gọi API)
        val savedName = AppContainer.authRepository.getManagerName()
        if (!savedName.isNullOrBlank()) {
            _uiState.update { it.copy(managerName = savedName, managerAvatarUrl = AppContainer.authRepository.getAvatarUrl()) }
        }
        loadStats()
        loadMonthlyRevenue()
    }

    fun onPeriodSelected(period: RevenuePeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            AppContainer.dashboardRepository.getStats()
                .onSuccess { dto ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            stats = DashboardStats(
                                revenue = dto.monthlyRevenue.toLong(),
                                revenueTrendPercent = if (dto.totalBookings > 0) dto.todayBookings * 100 / dto.totalBookings else 0,
                                bookingCount = dto.totalBookings,
                                occupancyRate = if (dto.totalFields > 0)
                                    (dto.activeFields * 100 / dto.totalFields) else 0,
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

    fun loadMonthlyRevenue() {
        viewModelScope.launch {
            AppContainer.dashboardRepository.getMonthlyRevenue().onSuccess { list ->
                if (list.isEmpty()) return@onSuccess
                val maxRevenue = list.maxOf { it.revenue }.takeIf { it > 0 } ?: 1.0
                val points = list.map { dto ->
                    WeeklyRevenuePoint("T${dto.month}", (dto.revenue / maxRevenue).toFloat())
                }
                _uiState.update { it.copy(monthlyRevenue = points) }
            }
        }
    }
}
