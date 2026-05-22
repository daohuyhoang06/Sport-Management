package com.sportmanagement.user.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sportmanagement.user.ui.components.chatbot.ChatbotOverlay
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.screens.BookingConfirmationScreen
import com.sportmanagement.user.ui.screens.HomeSearchFilterScreen
import com.sportmanagement.user.ui.screens.BookingPaymentScreen
import com.sportmanagement.user.ui.screens.BookingScheduleScreen
import com.sportmanagement.user.ui.screens.LoginScreen
import com.sportmanagement.user.ui.screens.RegisterScreen
import com.sportmanagement.user.ui.screens.UserFavoriteScreen
import com.sportmanagement.user.ui.screens.UserHomeScreen
import com.sportmanagement.user.ui.screens.UserMapScreen
import com.sportmanagement.user.ui.screens.UserProfileScreen
import com.sportmanagement.user.ui.viewmodel.ChatbotViewModel
import com.sportmanagement.user.ui.viewmodel.UserViewModel

@Composable
fun UserApp(
    userViewModel: UserViewModel = viewModel(),
    chatbotViewModel: ChatbotViewModel = viewModel()
) {
    val uiState by userViewModel.uiState.collectAsState()
    val chatbotUiState by chatbotViewModel.uiState.collectAsState()
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var showAuthScreen by rememberSaveable { mutableStateOf(false) }
    var showRegister by rememberSaveable { mutableStateOf(false) }
    var showBookingScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingConfirmationScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingPaymentScreen by rememberSaveable { mutableStateOf(false) }
    var showHomeSearchFilterScreen by rememberSaveable { mutableStateOf(false) }
    var bookingConfirmationData by remember { mutableStateOf<BookingConfirmationData?>(null) }

    val statusBarColor = when {
        showAuthScreen -> Color.Transparent
        showHomeSearchFilterScreen -> Color.Transparent
        showBookingScreen || showBookingPaymentScreen || showBookingConfirmationScreen -> Color.Transparent
        uiState.selectedTab == UserTab.Home || uiState.selectedTab == UserTab.Profile -> Color.Transparent
        else -> MaterialTheme.colorScheme.surface
    }
    val useDarkStatusBarIcons = !showAuthScreen &&
        !showHomeSearchFilterScreen &&
        !showBookingScreen &&
        !showBookingConfirmationScreen &&
        !showBookingPaymentScreen &&
        uiState.selectedTab != UserTab.Home &&
        uiState.selectedTab != UserTab.Profile
    val shouldShowBottomBar = !showBookingScreen &&
        !showBookingConfirmationScreen &&
        !showBookingPaymentScreen &&
        !showAuthScreen &&
        !showHomeSearchFilterScreen

    AppStatusBarEffect(
        statusBarColor = statusBarColor,
        useDarkIcons = useDarkStatusBarIcons
    )

    LaunchedEffect(shouldShowBottomBar) {
        chatbotViewModel.setWidgetEnabled(shouldShowBottomBar)
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                UserBottomBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = userViewModel::onTabSelected
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (showAuthScreen) {
                if (showRegister) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            showRegister = false
                            showAuthScreen = true
                        },
                        onNavigateToLogin = {
                            showRegister = false
                        }
                    )
                } else {
                    LoginScreen(
                        onLoginSuccess = {
                            isLoggedIn = true
                            showAuthScreen = false
                            showRegister = false
                        },
                        onNavigateToRegister = {
                            showRegister = true
                        },
                        onBackClick = {
                            showAuthScreen = false
                        }
                    )
                }
            } else if (showBookingPaymentScreen && bookingConfirmationData != null) {
                BookingPaymentScreen(
                    confirmationData = bookingConfirmationData!!,
                    userName = uiState.profile.name,
                    userPhone = uiState.profile.phone,
                    onBackClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = true
                    },
                    onConfirmBookingClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = false
                        showBookingScreen = false
                        bookingConfirmationData = null
                    },
                    onReturnHomeClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = false
                        showBookingScreen = false
                        bookingConfirmationData = null
                        userViewModel.onTabSelected(UserTab.Home)
                    }
                )
            } else if (showBookingConfirmationScreen && bookingConfirmationData != null) {
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
                        showBookingPaymentScreen = true
                    }
                )
            } else if (showHomeSearchFilterScreen) {
                HomeSearchFilterScreen(
                    filterOptions = uiState.homeSearchFilterOptions,
                    initialCriteria = uiState.activeHomeSearchCriteria,
                    onBackClick = { showHomeSearchFilterScreen = false },
                    onApplyFilters = { criteria ->
                        userViewModel.onApplyHomeSearchCriteria(criteria)
                        showHomeSearchFilterScreen = false
                    }
                )
            } else if (showBookingScreen) {
                BookingScheduleScreen(
                    scheduleData = uiState.bookingSchedule,
                    onBackClick = {
                        showBookingScreen = false
                        bookingConfirmationData = null
                    },
                    onNextClick = { confirmationData ->
                        bookingConfirmationData = confirmationData
                        showBookingScreen = false
                        showBookingPaymentScreen = false
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
                        isLoggedIn = isLoggedIn,
                        onLoginClick = {
                            showRegister = false
                            showAuthScreen = true
                        },
                        onRegisterClick = {
                            showRegister = true
                            showAuthScreen = true
                        },
                        onFilterClick = {
                            showHomeSearchFilterScreen = true
                        },
                        onBookFieldClick = { _ ->
                            showBookingPaymentScreen = false
                            showBookingConfirmationScreen = false
                            bookingConfirmationData = null
                            showBookingScreen = true
                        }
                    )
                    UserTab.Map -> UserMapScreen(
                        padding = padding,
                        sportCategories = uiState.sportCategories,
                        nearby = uiState.nearbyFields,
                        onBookFieldClick = { _ ->
                            showBookingPaymentScreen = false
                            showBookingConfirmationScreen = false
                            bookingConfirmationData = null
                            showBookingScreen = true
                        }
                    )
                    UserTab.Favorites -> UserFavoriteScreen(padding, uiState.favoriteFields)
                    UserTab.Profile -> UserProfileScreen(
                        padding = padding,
                        profile = uiState.profile,
                        isLoggedIn = isLoggedIn,
                        onLoginClick = {
                            showRegister = false
                            showAuthScreen = true
                        },
                        onRegisterClick = {
                            showRegister = true
                            showAuthScreen = true
                        },
                        onLogoutClick = {
                            isLoggedIn = false
                            showRegister = false
                            showAuthScreen = false
                        }
                    )
                }
            }

            ChatbotOverlay(
                uiState = chatbotUiState,
                contentPadding = padding,
                onToggleWindow = chatbotViewModel::toggleWindow,
                onCloseWindow = chatbotViewModel::closeWindow,
                onDraftChanged = chatbotViewModel::onDraftMessageChange,
                onSendMessage = chatbotViewModel::sendDraftMessage,
                onRetryMessage = chatbotViewModel::retryMessage,
                onDismissError = chatbotViewModel::dismissError,
                onButtonAnchorChanged = chatbotViewModel::onButtonAnchorChanged
            )
        }
    }
}
