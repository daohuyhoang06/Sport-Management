package com.sportmanagement.user.ui.state

import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat
import com.sportmanagement.user.ui.navigation.UserTab

data class UserUiState(
    val selectedTab: UserTab = UserTab.Home,
    val homeFields: List<UserField> = emptyList(),
    val sportCategories: List<SportCategory> = emptyList(),
    val mapCategories: List<String> = emptyList(),
    val nearbyFields: List<UserField> = emptyList(),
    val favoriteFields: List<UserField> = emptyList(),
    val bookingSchedule: BookingScheduleData = BookingScheduleData("", emptyList(), emptyList()),
    val profile: UserProfile = UserProfile(
        name = "",
        email = "",
        phone = "",
        membership = ""
    ),
    val stats: List<UserStat> = emptyList()
)
