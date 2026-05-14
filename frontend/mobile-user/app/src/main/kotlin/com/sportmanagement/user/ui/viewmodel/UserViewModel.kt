package com.sportmanagement.user.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sportmanagement.user.data.mock.MockUserRepository
import com.sportmanagement.user.domain.model.HomeSearchCriteria
import com.sportmanagement.user.domain.repository.UserRepository
import com.sportmanagement.user.domain.usecase.FilterHomeFieldsUseCase
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.state.UserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class UserViewModel(
    private val repository: UserRepository = MockUserRepository(),
    private val filterHomeFieldsUseCase: FilterHomeFieldsUseCase = FilterHomeFieldsUseCase()
) : ViewModel() {

    private val allHomeFields = repository.getHomeFields()
    private val homeSearchFilterOptions = repository.getHomeSearchFilterOptions()

    private val _uiState = MutableStateFlow(
        UserUiState(
            homeFields = allHomeFields,
            sportCategories = repository.getSportCategories(),
            mapCategories = repository.getMapCategories(),
            nearbyFields = repository.getNearbyFields(),
            favoriteFields = repository.getFavoriteFields(),
            homeSearchFilterOptions = homeSearchFilterOptions,
            bookingSchedule = repository.getBookingSchedule(),
            profile = repository.getProfile(),
            stats = repository.getStats()
        )
    )

    val uiState: StateFlow<UserUiState> = _uiState

    fun onTabSelected(tab: UserTab) {
        _uiState.update { current -> current.copy(selectedTab = tab) }
    }

    fun onApplyHomeSearchCriteria(criteria: HomeSearchCriteria) {
        _uiState.update { current ->
            current.copy(
                activeHomeSearchCriteria = criteria,
                homeFields = filterHomeFieldsUseCase(
                    fields = allHomeFields,
                    criteria = criteria,
                    options = homeSearchFilterOptions
                )
            )
        }
    }
}
