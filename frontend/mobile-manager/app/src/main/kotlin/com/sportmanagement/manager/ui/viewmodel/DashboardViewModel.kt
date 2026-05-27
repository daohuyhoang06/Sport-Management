package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sportmanagement.manager.domain.model.RevenuePeriod
import com.sportmanagement.manager.ui.state.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun onPeriodSelected(period: RevenuePeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }
}
