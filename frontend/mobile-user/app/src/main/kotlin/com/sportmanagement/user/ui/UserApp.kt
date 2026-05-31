package com.sportmanagement.user.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.domain.model.FieldDetail
import com.sportmanagement.user.domain.model.mockFieldDetail
import com.sportmanagement.user.ui.components.UserBottomBar
import com.sportmanagement.user.ui.components.chatbot.ChatbotOverlay
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.screens.BookingConfirmationScreen
import com.sportmanagement.user.ui.screens.HomeSearchFilterScreen
import com.sportmanagement.user.ui.screens.BookingPaymentScreen
import com.sportmanagement.user.ui.screens.BookingScheduleScreen
<<<<<<< HEAD
import com.sportmanagement.user.ui.screens.FieldDetailScreen
=======
import com.sportmanagement.user.ui.screens.BookingDetailScreen
import com.sportmanagement.user.ui.screens.ConversationScreen
import com.sportmanagement.user.ui.screens.BookingInfo
import com.sportmanagement.user.ui.screens.ConversationInfo
import com.sportmanagement.user.ui.screens.NotificationDetailScreen
import com.sportmanagement.user.ui.screens.NotificationDetailInfo
>>>>>>> origin/develop
import com.sportmanagement.user.ui.screens.LoginScreen
import com.sportmanagement.user.ui.screens.RegisterScreen
import com.sportmanagement.user.ui.screens.InboxScreen
import com.sportmanagement.user.ui.screens.HomeSearchResultsScreen
import com.sportmanagement.user.ui.screens.UserHomeScreen
import com.sportmanagement.user.ui.screens.UserMapScreen
import com.sportmanagement.user.ui.screens.UserProfileScreen
import com.sportmanagement.user.ui.viewmodel.ChatbotViewModel
import com.sportmanagement.user.ui.viewmodel.UserViewModel
import com.sportmanagement.user.ui.viewmodel.UserViewModelFactory

@Composable
fun UserApp(
    userViewModel: UserViewModel? = null,
    chatbotViewModel: ChatbotViewModel = viewModel()
) {
    val appContext = LocalContext.current.applicationContext
    val resolvedUserViewModel = userViewModel ?: viewModel(
        factory = remember(appContext) { UserViewModelFactory(appContext) }
    )
    val uiState by resolvedUserViewModel.uiState.collectAsState()
    val chatbotUiState by chatbotViewModel.uiState.collectAsState()
    var showAuthScreen by rememberSaveable { mutableStateOf(false) }
    var showRegister by rememberSaveable { mutableStateOf(false) }
    var selectedFieldId by rememberSaveable { mutableStateOf<Int?>(null) }
    var showBookingScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingConfirmationScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingPaymentScreen by rememberSaveable { mutableStateOf(false) }
    var showHomeSearchFilterScreen by rememberSaveable { mutableStateOf(false) }
    var showHomeSearchResultsScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingDetailScreen by rememberSaveable { mutableStateOf(false) }
    var showConversationScreen by rememberSaveable { mutableStateOf(false) }
    var showNotificationDetailScreen by rememberSaveable { mutableStateOf(false) }
    var bookingConfirmationData by remember { mutableStateOf<BookingConfirmationData?>(null) }
<<<<<<< HEAD
    var selectedFieldDetail by remember { mutableStateOf<FieldDetail?>(null) }
=======
    var bookingDetailInfo by remember { mutableStateOf<BookingInfo?>(null) }
    var conversationInfo by remember { mutableStateOf<ConversationInfo?>(null) }
    var notificationDetailInfo by remember { mutableStateOf<NotificationDetailInfo?>(null) }
>>>>>>> origin/develop

    val closeHomeSearchResultsFlow = {
        showHomeSearchResultsScreen = false
        resolvedUserViewModel.resetHomeSearchCriteria()
    }

    if (selectedFieldDetail != null) {
        AppStatusBarEffect(statusBarColor = Color.Transparent, useDarkIcons = false)
        FieldDetailScreen(
            fieldDetail = selectedFieldDetail!!,
            onBackClick = { selectedFieldDetail = null },
            onBookNowClick = { detail ->
                selectedCourtName = detail.name
                selectedFieldDetail = null
                showBookingConfirmationScreen = false
                bookingConfirmationData = null
                showBookingScreen = true
            }
        )
        return
    }

    val statusBarColor = when {
        showAuthScreen -> Color.Transparent
        showHomeSearchFilterScreen -> Color.Transparent
        showHomeSearchResultsScreen -> Color.Transparent
        showBookingScreen || showBookingPaymentScreen || showBookingConfirmationScreen -> Color.Transparent
        showBookingDetailScreen || showConversationScreen || showNotificationDetailScreen -> MaterialTheme.colorScheme.surface
        uiState.selectedTab == UserTab.Home || uiState.selectedTab == UserTab.Profile -> Color.Transparent
        else -> MaterialTheme.colorScheme.surface
    }
    val useDarkStatusBarIcons = !showAuthScreen &&
        !showHomeSearchFilterScreen &&
        !showHomeSearchResultsScreen &&
        !showBookingScreen &&
        !showBookingConfirmationScreen &&
        !showBookingPaymentScreen &&
        !showBookingDetailScreen &&
        !showConversationScreen &&
        !showNotificationDetailScreen &&
        uiState.selectedTab != UserTab.Home &&
        uiState.selectedTab != UserTab.Profile
    val shouldShowBottomBar = !showBookingScreen &&
        !showBookingConfirmationScreen &&
        !showBookingPaymentScreen &&
        !showBookingDetailScreen &&
        !showConversationScreen &&
        !showNotificationDetailScreen &&
        !showAuthScreen &&
        !showHomeSearchFilterScreen &&
        !showHomeSearchResultsScreen

    AppStatusBarEffect(
        statusBarColor = statusBarColor,
        useDarkIcons = useDarkStatusBarIcons
    )

    LaunchedEffect(shouldShowBottomBar) {
        chatbotViewModel.setWidgetEnabled(shouldShowBottomBar)
    }

    LaunchedEffect(uiState.isAuthenticated, showAuthScreen) {
        if (uiState.isAuthenticated && showAuthScreen) {
            showAuthScreen = false
            showRegister = false
        }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                UserBottomBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = resolvedUserViewModel::onTabSelected
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (showAuthScreen) {
                if (showRegister) {
                    RegisterScreen(
                        isLoading = uiState.isAuthLoading,
                        errorMessage = uiState.authError,
                        onRegisterSubmit = { formState, selectedSportTypeKeys ->
                            resolvedUserViewModel.register(
                                fullName = formState.fullName,
                                email = formState.email,
                                password = formState.password,
                                phone = formState.phone,
                                birthday = formState.birthDate.takeIf { it.contains("/") },
                                address = null,
                                preferredSportTypeKeys = selectedSportTypeKeys
                            )
                        },
                        onNavigateToLogin = {
                            resolvedUserViewModel.clearAuthError()
                            showRegister = false
                        }
                    )
                } else {
                    LoginScreen(
                        isLoading = uiState.isAuthLoading,
                        errorMessage = uiState.authError,
                        onLoginSubmit = { identifier, password ->
                            resolvedUserViewModel.login(
                                identifier = identifier,
                                password = password
                            )
                        },
                        onGoogleLoginSubmit = { idToken ->
                            resolvedUserViewModel.loginWithGoogle(idToken)
                        },
                        onNavigateToRegister = {
                            resolvedUserViewModel.clearAuthError()
                            showRegister = true
                        },
                        onBackClick = {
                            resolvedUserViewModel.clearAuthError()
                            showAuthScreen = false
                        }
                    )
                }
            } else if (showBookingPaymentScreen && bookingConfirmationData != null) {
                BookingPaymentScreen(
                    confirmationData = bookingConfirmationData!!,
                    userName = uiState.profile.name,
<<<<<<< HEAD
                    onBookFieldClick = { field ->
                        // Mở FieldDetailScreen trước, user sẽ bấm "Đặt sân ngay" từ đó
                        selectedFieldDetail = mockFieldDetail().copy(name = field.name)
=======
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
                        closeHomeSearchResultsFlow()
                        resolvedUserViewModel.onTabSelected(UserTab.Home)
>>>>>>> origin/develop
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
            } else if (showBookingDetailScreen && bookingDetailInfo != null) {
                BookingDetailScreen(
                    info = bookingDetailInfo!!,
                    onBackClick = { showBookingDetailScreen = false },
                    onOpenChat = { info ->
                        conversationInfo = info
                        showBookingDetailScreen = false
                        showConversationScreen = true
                    }
                )
            } else if (showConversationScreen && conversationInfo != null) {
                ConversationScreen(
                    info = conversationInfo!!,
                    onBackClick = { showConversationScreen = false }
                )
            } else if (showNotificationDetailScreen && notificationDetailInfo != null) {
                NotificationDetailScreen(
                    info = notificationDetailInfo!!,
                    onBackClick = { showNotificationDetailScreen = false },
                    onOpenChat = { info ->
                        conversationInfo = info
                        showNotificationDetailScreen = false
                        showConversationScreen = true
                    },
                    onPromotionAction = {
                        showNotificationDetailScreen = false
                        resolvedUserViewModel.onTabSelected(UserTab.Map)
                    }
                )
            } else if (showHomeSearchFilterScreen) {
                HomeSearchFilterScreen(
                    filterOptions = uiState.homeSearchFilterOptions,
                    initialCriteria = uiState.activeHomeSearchCriteria,
                    onBackClick = { showHomeSearchFilterScreen = false },
                    onApplyFilters = { criteria ->
                        resolvedUserViewModel.onApplyHomeSearchCriteria(criteria)
                        showHomeSearchFilterScreen = false
                        showHomeSearchResultsScreen = true
                    }
                )
            } else if (showBookingScreen && selectedFieldId != null) {
                val todayStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
                BookingScheduleScreen(
                    fieldId = selectedFieldId!!,
                    initialDateText = todayStr,
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
                    UserTab.Home -> if (showHomeSearchResultsScreen) {
                        HomeSearchResultsScreen(
                            padding = padding,
                            fields = uiState.homeFields,
                            isLoading = uiState.isHomeLoading,
                            onBackClick = closeHomeSearchResultsFlow,
                            onFilterClick = {
                                showHomeSearchFilterScreen = true
                            },
                            onBookFieldClick = { field ->
                                selectedFieldId = field.fieldId
                                showBookingPaymentScreen = false
                                showBookingConfirmationScreen = false
                                bookingConfirmationData = null
                                showBookingScreen = true
                            }
                        )
                    } else {
                        UserHomeScreen(
                            padding = padding,
                            fields = uiState.homeFields,
                            sportCategories = uiState.sportCategories,
                            userName = uiState.profile.name,
                            userAvatarUrl = uiState.profile.avatarUrl,
                            isLoggedIn = uiState.isAuthenticated,
                            isInitialLoading = uiState.isHomeLoading,
                            isLoadingMore = uiState.isHomeLoadingMore,
                            hasMoreData = uiState.hasMoreHomeFields,
                            searchResults = uiState.fieldSearchResults,
                            recentSearches = uiState.recentFieldSearches,
                            isSearchLoading = uiState.isFieldSearchLoading,
                            isSearchLoadingMore = uiState.isFieldSearchLoadingMore,
                            hasMoreSearchResults = uiState.hasMoreFieldSearchResults,
                            onLoginClick = {
                                resolvedUserViewModel.clearAuthError()
                                showRegister = false
                                showAuthScreen = true
                            },
                            onRegisterClick = {
                                resolvedUserViewModel.clearAuthError()
                                showRegister = true
                                showAuthScreen = true
                            },
                            onFilterClick = {
                                showHomeSearchFilterScreen = true
                            },
                            onSearchOpened = {
                                resolvedUserViewModel.onFieldSearchOpened()
                            },
                            onSearchRequest = { keyword, address, sportType ->
                                resolvedUserViewModel.searchFields(
                                    keyword = keyword,
                                    address = address,
                                    sportType = sportType
                                )
                            },
                            onClearSearch = {
                                resolvedUserViewModel.clearFieldSearchResults()
                            },
                            onLoadMoreSearchResults = {
                                resolvedUserViewModel.loadMoreFieldSearchResults()
                            },
                            onRememberSearch = { query ->
                                resolvedUserViewModel.rememberFieldSearch(query)
                            },
                            onCurrentLocationDetected = { latitude, longitude ->
                                resolvedUserViewModel.onHomeLocationUpdated(latitude, longitude)
                            },
                            onLocationUnavailable = {
                                resolvedUserViewModel.onHomeLocationUnavailable()
                            },
                            onLoadMore = {
                                resolvedUserViewModel.loadMoreHomeFields()
                            },
                            onBookFieldClick = { field ->
                                selectedFieldId = field.fieldId
                                showBookingPaymentScreen = false
                                showBookingConfirmationScreen = false
                                bookingConfirmationData = null
                                showBookingScreen = true
                            }
                        )
                    }
                    UserTab.Map -> UserMapScreen(
                        padding = padding,
                        sportCategories = uiState.sportCategories,
                        nearby = uiState.nearbyFields,
                        searchResults = uiState.fieldSearchResults,
                        recentSearches = uiState.recentFieldSearches,
                        isSearchLoading = uiState.isFieldSearchLoading,
                        isSearchLoadingMore = uiState.isFieldSearchLoadingMore,
                        hasMoreSearchResults = uiState.hasMoreFieldSearchResults,
                        onSearchOpened = {
                            resolvedUserViewModel.onFieldSearchOpened()
                        },
                        onSearchRequest = { keyword, address, sportType ->
                            resolvedUserViewModel.searchFields(
                                keyword = keyword,
                                address = address,
                                sportType = sportType
                            )
                        },
                        onClearSearch = {
                            resolvedUserViewModel.clearFieldSearchResults()
                        },
                        onLoadMoreSearchResults = {
                            resolvedUserViewModel.loadMoreFieldSearchResults()
                        },
                        onRememberSearch = { query ->
                            resolvedUserViewModel.rememberFieldSearch(query)
                        },
                        onCurrentLocationDetected = { latitude, longitude ->
                            resolvedUserViewModel.onHomeLocationUpdated(latitude, longitude)
                        },
                        onBookFieldClick = { field ->
                            selectedFieldId = field.fieldId
                            showBookingPaymentScreen = false
                            showBookingConfirmationScreen = false
                            bookingConfirmationData = null
                            showBookingScreen = true
                        }
                    )
                    UserTab.Inbox -> InboxScreen(
                        padding = padding,
                        onBookingSelected = { info ->
                            bookingDetailInfo = info
                            showBookingDetailScreen = true
                        },
                        onMessageSelected = { info ->
                            conversationInfo = info
                            showConversationScreen = true
                        },
                        onNotificationSelected = { info ->
                            notificationDetailInfo = info
                            showNotificationDetailScreen = true
                        }
                    )
                    UserTab.Profile -> UserProfileScreen(
                        padding = padding,
                        profile = uiState.profile,
                        isLoggedIn = uiState.isAuthenticated,
                        onLoginClick = {
                            resolvedUserViewModel.clearAuthError()
                            showRegister = false
                            showAuthScreen = true
                        },
                        onRegisterClick = {
                            resolvedUserViewModel.clearAuthError()
                            showRegister = true
                            showAuthScreen = true
                        },
                        onLogoutClick = {
                            resolvedUserViewModel.logout()
                            showRegister = false
                            showAuthScreen = false
                        },
                        onProfileUpdate = { updatedProfile ->
                            resolvedUserViewModel.updateProfile(updatedProfile)
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
