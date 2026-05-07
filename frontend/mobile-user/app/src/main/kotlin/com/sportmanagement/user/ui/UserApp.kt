package com.sportmanagement.user.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.ui.components.UserBottomBar
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.screens.BookingConfirmationScreen
import com.sportmanagement.user.ui.screens.BookingScheduleScreen
import com.sportmanagement.user.ui.screens.LoginScreen
import com.sportmanagement.user.ui.screens.RegisterScreen
import com.sportmanagement.user.ui.screens.UserFavoriteScreen
import com.sportmanagement.user.ui.screens.UserHomeScreen
import com.sportmanagement.user.ui.screens.UserMapScreen
import com.sportmanagement.user.ui.screens.UserProfileScreen
import com.sportmanagement.user.ui.viewmodel.UserViewModel

@Composable
fun UserApp(userViewModel: UserViewModel = viewModel()) {
    val uiState by userViewModel.uiState.collectAsState()
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var showRegister by rememberSaveable { mutableStateOf(false) }
    var showBookingScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingConfirmationScreen by rememberSaveable { mutableStateOf(false) }
    var selectedCourtName by rememberSaveable { mutableStateOf("") }
    var bookingConfirmationData by remember { mutableStateOf<BookingConfirmationData?>(null) }

    if (!isAuthenticated) {
        AppStatusBarEffect(
            statusBarColor = Color.Transparent,
            useDarkIcons = false
        )

        if (showRegister) {
            RegisterScreen(
                onRegisterSuccess = {
                    showRegister = false
                },
                onNavigateToLogin = {
                    showRegister = false
                }
            )
        } else {
            LoginScreen(
                onLoginSuccess = {
                    isAuthenticated = true
                },
                onNavigateToRegister = {
                    showRegister = true
                }
            )
        }
        return
    }

    val statusBarColor = when {
        showBookingScreen || showBookingConfirmationScreen -> MaterialTheme.colorScheme.primary
        uiState.selectedTab == UserTab.Home -> Color.Transparent
        else -> MaterialTheme.colorScheme.surface
    }
    val useDarkStatusBarIcons = !showBookingScreen && !showBookingConfirmationScreen && uiState.selectedTab != UserTab.Home

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
            if (!showBookingScreen && !showBookingConfirmationScreen) {
                UserBottomBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = userViewModel::onTabSelected
                )
            }
        }
    ) { padding ->
        if (showBookingConfirmationScreen && bookingConfirmationData != null) {
            BookingConfirmationScreen(
                confirmationData = bookingConfirmationData!!,
                userName = uiState.profile.name,
                userPhone = uiState.profile.phone,
                onBackClick = {
                    showBookingConfirmationScreen = false
                    showBookingScreen = true
                },
                onConfirmPaymentClick = {
                    showBookingConfirmationScreen = false
                    showBookingScreen = false
                    bookingConfirmationData = null
                }
            )
        } else if (showBookingScreen) {
            BookingScheduleScreen(
                scheduleData = bookingData,
                onBackClick = {
                    showBookingScreen = false
                    bookingConfirmationData = null
                },
                onNextClick = { confirmationData ->
                    bookingConfirmationData = confirmationData
                    showBookingScreen = false
                    showBookingConfirmationScreen = true
                }
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
                        showBookingConfirmationScreen = false
                        bookingConfirmationData = null
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
