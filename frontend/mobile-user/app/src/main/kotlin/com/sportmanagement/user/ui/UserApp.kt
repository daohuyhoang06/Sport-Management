package com.sportmanagement.user.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.user.ui.components.UserBottomBar
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.screens.UserFavoriteScreen
import com.sportmanagement.user.ui.screens.UserHomeScreen
import com.sportmanagement.user.ui.screens.UserMapScreen
import com.sportmanagement.user.ui.screens.UserProfileScreen
import com.sportmanagement.user.ui.viewmodel.UserViewModel

@Composable
fun UserApp(userViewModel: UserViewModel = viewModel()) {
    val uiState by userViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            UserBottomBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = userViewModel::onTabSelected
            )
        }
    ) { padding ->
        when (uiState.selectedTab) {
            UserTab.Home -> UserHomeScreen(
                padding = padding,
                fields = uiState.homeFields,
                sportCategories = uiState.sportCategories,
                userName = uiState.profile.name
            )
            UserTab.Map -> UserMapScreen(padding, uiState.sportCategories, uiState.nearbyFields)
            UserTab.Favorites -> UserFavoriteScreen(padding, uiState.favoriteFields)
            UserTab.Profile -> UserProfileScreen(padding, uiState.profile, uiState.stats)
        }
    }
}
