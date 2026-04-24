package com.sportmanagement.user.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sportmanagement.user.data.MockUserRepository
import com.sportmanagement.user.data.UserRepository
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.state.UserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class UserViewModel(
    private val repository: UserRepository = MockUserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UserUiState(
            homeFields = repository.getHomeFields(),
            sportCategories = repository.getSportCategories(),
            mapCategories = repository.getMapCategories(),
            nearbyFields = repository.getNearbyFields(),
            favoriteFields = repository.getFavoriteFields(),
            profile = repository.getProfile(),
            stats = repository.getStats()
        )
    )

    val uiState: StateFlow<UserUiState> = _uiState

    fun onTabSelected(tab: UserTab) {
        _uiState.update { current -> current.copy(selectedTab = tab) }
    }
}
