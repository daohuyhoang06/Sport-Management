package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.Pitch
import com.sportmanagement.manager.domain.model.PitchStatus

data class PitchesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pitches: List<Pitch> = emptyList(),
    val searchQuery: String = "",
    val showFilterDialog: Boolean = false,
    val filterStatus: PitchStatus? = null,
    val filterSportType: String? = null,
    val filterMaxPrice: Long? = null,
    val showAddField: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null
) {
    val filteredPitches: List<Pitch>
        get() = pitches.filter { pitch ->
            val matchSearch = pitch.name.contains(searchQuery, ignoreCase = true) ||
                pitch.location.contains(searchQuery, ignoreCase = true)
            val matchStatus = filterStatus == null || pitch.status == filterStatus
            val matchPrice = filterMaxPrice == null || pitch.pricePerHour <= filterMaxPrice
            matchSearch && matchStatus && matchPrice
        }

    val hasActiveFilter: Boolean
        get() = filterStatus != null || filterSportType != null || filterMaxPrice != null
}
