package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.data.mapper.toPitch
import com.sportmanagement.manager.data.remote.dto.CreateFieldRequest
import com.sportmanagement.manager.domain.model.PitchStatus
import com.sportmanagement.manager.ui.state.PitchesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

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

    fun onToggleAddField() {
        _uiState.update { it.copy(showAddField = !it.showAddField, saveError = null) }
    }

    fun createField(
        fieldName: String,
        location: String,
        sportId: Int,
        phone: String?,
        openTime: String?,
        closeTime: String?,
        slotPrice: Double?,
        slotMinutes: Int?,
        status: String,
        latitude: Double?,
        longitude: Double?,
        avatarImagePart: MultipartBody.Part?,
        cardImagePart: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            var avatarUrl: String? = null
            var cardUrl: String? = null

            if (avatarImagePart != null) {
                AppContainer.fieldRepository.uploadFieldImage(avatarImagePart)
                    .onSuccess { avatarUrl = it }
                    .onFailure { e ->
                        _uiState.update { s -> s.copy(isSaving = false, saveError = "Upload ảnh đại diện thất bại: ${e.message}") }
                        return@launch
                    }
            }

            if (cardImagePart != null) {
                AppContainer.fieldRepository.uploadFieldImage(cardImagePart)
                    .onSuccess { cardUrl = it }
                    .onFailure { e ->
                        _uiState.update { s -> s.copy(isSaving = false, saveError = "Upload ảnh bìa thất bại: ${e.message}") }
                        return@launch
                    }
            }

            val request = CreateFieldRequest(
                fieldName = fieldName,
                location = location,
                sportId = sportId,
                phone = phone?.takeIf { it.isNotBlank() },
                openTime = openTime?.takeIf { it.isNotBlank() },
                closeTime = closeTime?.takeIf { it.isNotBlank() },
                slotPrice = slotPrice,
                slotMinutes = slotMinutes,
                status = status,
                latitude = latitude,
                longitude = longitude,
                avatarImageUrl = avatarUrl,
                cardImageUrl = cardUrl
            )
            AppContainer.fieldRepository.createField(request)
                .onSuccess { dto ->
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            showAddField = false,
                            pitches = listOf(dto.toPitch()) + state.pitches
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, saveError = e.message) }
                }
        }
    }
}
