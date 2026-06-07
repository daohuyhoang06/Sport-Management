package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.data.mapper.toPitch
import com.sportmanagement.manager.domain.model.PitchStatus
import com.sportmanagement.manager.ui.state.PitchesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PitchesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PitchesUiState())
    val uiState: StateFlow<PitchesUiState> = _uiState

    init {
        loadFields()
    }

    fun loadFields() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            AppContainer.fieldRepository.getFields().fold(
                onSuccess = { dtos ->
                    _uiState.update { it.copy(isLoading = false, pitches = dtos.map { dto -> dto.toPitch() }) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onToggleFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = !it.showFilterDialog) }
    }

    fun onFilterStatusChanged(status: PitchStatus?) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    fun onFilterMaxPriceChanged(price: Long?) {
        _uiState.update { it.copy(filterMaxPrice = price) }
    }

    fun onClearFilters() {
        _uiState.update { it.copy(filterStatus = null, filterSportType = null, filterMaxPrice = null) }
    }

    fun onApplyFilters() {
        _uiState.update { it.copy(showFilterDialog = false) }
    }
}
