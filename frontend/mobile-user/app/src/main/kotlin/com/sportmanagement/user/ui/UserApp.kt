package com.sportmanagement.user.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.user.ui.components.UserBottomBar
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.screens.BookingScheduleScreen
import com.sportmanagement.user.ui.screens.UserFavoriteScreen
import com.sportmanagement.user.ui.screens.UserHomeScreen
import com.sportmanagement.user.ui.screens.UserMapScreen
import com.sportmanagement.user.ui.screens.UserProfileScreen
import com.sportmanagement.user.ui.viewmodel.UserViewModel

@Composable
fun UserApp(userViewModel: UserViewModel = viewModel()) {
    val uiState by userViewModel.uiState.collectAsState()
    var showBookingScreen by rememberSaveable { mutableStateOf(false) }
    var selectedCourtName by rememberSaveable { mutableStateOf("") }
    val statusBarColor = when {
        showBookingScreen -> MaterialTheme.colorScheme.primary
        uiState.selectedTab == UserTab.Home -> Color.Transparent
        else -> MaterialTheme.colorScheme.surface
    }
    val useDarkStatusBarIcons = !showBookingScreen && uiState.selectedTab != UserTab.Home

    AppStatusBarEffect(
        statusBarColor = statusBarColor,
        useDarkIcons = useDarkStatusBarIcons
    )

    val bookingData = uiState.bookingSchedule.let { schedule ->
        if (selectedCourtName.isBlank()) schedule
        else schedule.copy(selectedCourtName = selectedCourtName)
    }

    Scaffold(
        bottomBar = {
            if (!showBookingScreen) {
                UserBottomBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = userViewModel::onTabSelected
                )
            }
        }
    ) { padding ->
        if (showBookingScreen) {
            BookingScheduleScreen(
                scheduleData = bookingData,
                onBackClick = { showBookingScreen = false },
                onNextClick = {}
            )
        } else {
            when (uiState.selectedTab) {
                UserTab.Home -> UserHomeScreen(
                    padding = padding,
                    fields = uiState.homeFields,
                    sportCategories = uiState.sportCategories,
                    userName = uiState.profile.name,
                    onBookFieldClick = { field ->
                        selectedCourtName = field.name
                        showBookingScreen = true
                    }
                )
                UserTab.Map -> UserMapScreen(padding, uiState.sportCategories, uiState.nearbyFields)
                UserTab.Favorites -> UserFavoriteScreen(padding, uiState.favoriteFields)
                UserTab.Profile -> UserProfileScreen(padding, uiState.profile, uiState.stats)
            }
        }
    }
}
