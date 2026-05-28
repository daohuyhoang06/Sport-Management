package com.sportmanagement.user.ui.state

import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.HomeSearchCriteria
import com.sportmanagement.user.domain.model.HomeSearchFilterOptions
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat
import com.sportmanagement.user.ui.navigation.UserTab

data class UserUiState(
    val selectedTab: UserTab = UserTab.Home,
    val isAuthenticated: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authError: String? = null,
    val homeFields: List<UserField> = emptyList(),
    val isHomeLoading: Boolean = false,
    val isHomeLoadingMore: Boolean = false,
    val hasMoreHomeFields: Boolean = true,
    val fieldSearchResults: List<UserField> = emptyList(),
    val recentFieldSearches: List<String> = emptyList(),
    val isFieldSearchLoading: Boolean = false,
    val isFieldSearchLoadingMore: Boolean = false,
    val hasMoreFieldSearchResults: Boolean = true,
    val sportCategories: List<SportCategory> = emptyList(),
    val mapCategories: List<String> = emptyList(),
    val nearbyFields: List<UserField> = emptyList(),
    val favoriteFields: List<UserField> = emptyList(),
    val homeSearchFilterOptions: HomeSearchFilterOptions = HomeSearchFilterOptions(),
    val activeHomeSearchCriteria: HomeSearchCriteria = HomeSearchCriteria(),
    val bookingSchedule: BookingScheduleData = BookingScheduleData(""),
    val profile: UserProfile = UserProfile(
        name = "",
        email = "",
        phone = "",
        membership = "Đồng"
    ),
    val stats: List<UserStat> = emptyList()
)
