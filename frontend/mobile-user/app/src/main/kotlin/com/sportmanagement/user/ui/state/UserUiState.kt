package com.sportmanagement.user.ui.state

import com.sportmanagement.user.ui.model.SportCategory
import com.sportmanagement.user.ui.model.UserField
import com.sportmanagement.user.ui.model.UserProfile
import com.sportmanagement.user.ui.model.UserStat
import com.sportmanagement.user.ui.navigation.UserTab

data class UserUiState(
    val selectedTab: UserTab = UserTab.Home,
    val homeFields: List<UserField> = emptyList(),
    val sportCategories: List<SportCategory> = emptyList(),
    val mapCategories: List<String> = emptyList(),
    val nearbyFields: List<UserField> = emptyList(),
    val favoriteFields: List<UserField> = emptyList(),
    val profile: UserProfile = UserProfile("", "", "", ""),
    val stats: List<UserStat> = emptyList()
)
