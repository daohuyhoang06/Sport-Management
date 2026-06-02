package com.sportmanagement.user.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.ui.components.UserBottomBar
import com.sportmanagement.user.ui.components.chatbot.ChatbotOverlay
import com.sportmanagement.user.ui.components.share.FieldShareSheet
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.screens.BookingConfirmationScreen
import com.sportmanagement.user.ui.screens.HomeSearchFilterScreen
import com.sportmanagement.user.ui.screens.BookingPaymentScreen
import com.sportmanagement.user.ui.screens.BookingScheduleScreen
import com.sportmanagement.user.ui.screens.BookingDetailScreen
import com.sportmanagement.user.ui.screens.ConversationScreen
import com.sportmanagement.user.ui.screens.BookingInfo
import com.sportmanagement.user.ui.screens.ConversationInfo
import com.sportmanagement.user.ui.screens.NotificationDetailScreen
import com.sportmanagement.user.ui.screens.NotificationDetailInfo
import com.sportmanagement.user.ui.screens.LoginScreen
import com.sportmanagement.user.ui.screens.RegisterScreen
import com.sportmanagement.user.ui.screens.InboxScreen
import com.sportmanagement.user.ui.screens.HomeSearchResultsScreen
import com.sportmanagement.user.ui.screens.UserHomeScreen
import com.sportmanagement.user.ui.screens.UserMapScreen
import com.sportmanagement.user.ui.screens.UserProfileScreen
import com.sportmanagement.user.ui.share.FieldShareLink
import com.sportmanagement.user.ui.share.FieldShareLink.MomoPaymentReturn
import com.sportmanagement.user.ui.viewmodel.ChatbotViewModel
import com.sportmanagement.user.ui.viewmodel.InboxViewModel
import com.sportmanagement.user.ui.viewmodel.InboxViewModelFactory
import com.sportmanagement.user.ui.viewmodel.UserViewModel
import com.sportmanagement.user.ui.viewmodel.UserViewModelFactory
import androidx.compose.runtime.mutableIntStateOf

@Composable
fun UserApp(
    userViewModel: UserViewModel? = null,
    chatbotViewModel: ChatbotViewModel = viewModel(),
    incomingDeepLinkFieldId: Int? = null,
    onDeepLinkConsumed: () -> Unit = {},
    incomingMomoPaymentReturn: MomoPaymentReturn? = null,
    onMomoPaymentReturnConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val appContext = context.applicationContext
    val resolvedUserViewModel = userViewModel ?: viewModel(
        factory = remember(appContext) { UserViewModelFactory(appContext) }
    )
    val inboxViewModel: InboxViewModel = viewModel(
        factory = remember(appContext) { InboxViewModelFactory(appContext) }
    )
    val uiState by resolvedUserViewModel.uiState.collectAsState()
    val inboxUiState by inboxViewModel.uiState.collectAsState()
    val chatbotUiState by chatbotViewModel.uiState.collectAsState()
    var showAuthScreen by rememberSaveable { mutableStateOf(false) }
    var showRegister by rememberSaveable { mutableStateOf(false) }
    var selectedFieldId by rememberSaveable { mutableStateOf<Int?>(null) }
    var bookingSessionKey by rememberSaveable { mutableIntStateOf(0) }
    var showBookingScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingConfirmationScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingPaymentScreen by rememberSaveable { mutableStateOf(false) }
    var showHomeSearchFilterScreen by rememberSaveable { mutableStateOf(false) }
    var showHomeSearchResultsScreen by rememberSaveable { mutableStateOf(false) }
    var showFavoriteFieldsScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingDetailScreen by rememberSaveable { mutableStateOf(false) }
    var showConversationScreen by rememberSaveable { mutableStateOf(false) }
    var showNotificationDetailScreen by rememberSaveable { mutableStateOf(false) }
    var bookingContactName by rememberSaveable { mutableStateOf("") }
    var bookingContactPhone by rememberSaveable { mutableStateOf("") }
    var bookingConfirmationData by remember { mutableStateOf<BookingConfirmationData?>(null) }
    var selectedBookingField by remember { mutableStateOf<UserField?>(null) }
    var bookingDetailInfo by remember { mutableStateOf<BookingInfo?>(null) }
    var conversationInfo by remember { mutableStateOf<ConversationInfo?>(null) }
    var notificationDetailInfo by remember { mutableStateOf<NotificationDetailInfo?>(null) }
    var fieldToShare by remember { mutableStateOf<UserField?>(null) }
    var pendingDeepLinkFieldId by rememberSaveable { mutableStateOf<Int?>(null) }

    val closeHomeSearchResultsFlow = {
        showHomeSearchResultsScreen = false
        resolvedUserViewModel.resetHomeSearchCriteria()
    }
    val closeFavoriteFieldsFlow = {
        showFavoriteFieldsScreen = false
    }
    val closeHomeOverlayFlows = {
        closeHomeSearchResultsFlow()
        closeFavoriteFieldsFlow()
    }
    val openAuthFlow = {
        resolvedUserViewModel.clearAuthError()
        showRegister = false
        showAuthScreen = true
    }
    val startBookingFlow: (UserField) -> Unit = { field ->
        selectedBookingField = field
        selectedFieldId = field.fieldId
        bookingSessionKey += 1
        bookingContactName = uiState.profile.name
        bookingContactPhone = uiState.profile.phone
        showBookingPaymentScreen = false
        showBookingConfirmationScreen = false
        bookingConfirmationData = null
        showBookingScreen = true
    }
    val toggleFavoriteField: (UserField, Boolean) -> Unit = { field, isFavorite ->
        if (!uiState.isAuthenticated) {
            openAuthFlow()
        } else {
            resolvedUserViewModel.setFieldFavorite(field, isFavorite)
        }
    }
    val shareField: (UserField) -> Unit = { field ->
        fieldToShare = field
    }

    val selectedFieldShareUrl = remember(fieldToShare) {
        fieldToShare?.fieldId?.takeIf { it > 0 }?.let(FieldShareLink::webFieldLink)
    }
    val selectedFieldShareText = remember(fieldToShare, selectedFieldShareUrl) {
        val field = fieldToShare ?: return@remember null
        val link = selectedFieldShareUrl.orEmpty()
        buildString {
            append(context.getString(R.string.share_text_prefix, context.getString(R.string.app_name)))
            append('\n')
            append(context.getString(R.string.share_text_field_label, field.name))
            append('\n')
            append(context.getString(R.string.share_text_location_label, field.location))
            if (field.hours.isNotBlank()) {
                append('\n')
                append(context.getString(R.string.share_text_hours_label, field.hours))
            }
            if (field.price.isNotBlank()) {
                append('\n')
                append(context.getString(R.string.share_text_price_label, field.price))
            }
            if (link.isNotBlank()) {
                append('\n')
                append('\n')
                append(link)
            }
        }
    }
    val performShareNow: () -> Unit = {
        val field = fieldToShare
        val shareText = selectedFieldShareText
        if (field != null && shareText != null) {
            try {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        },
                        null
                    )
                )
                fieldToShare = null
            } catch (_: Exception) {
                Toast.makeText(context, context.getString(R.string.share_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(incomingDeepLinkFieldId) {
        val id = incomingDeepLinkFieldId ?: return@LaunchedEffect
        pendingDeepLinkFieldId = id
        showHomeSearchFilterScreen = false
        showHomeSearchResultsScreen = false
        showFavoriteFieldsScreen = false
        showBookingDetailScreen = false
        showConversationScreen = false
        showNotificationDetailScreen = false
        showBookingScreen = false
        showBookingConfirmationScreen = false
        showBookingPaymentScreen = false
        selectedFieldId = null
        bookingConfirmationData = null
        selectedBookingField = null
        resolvedUserViewModel.onTabSelected(UserTab.Home)
    }

    val statusBarColor = when {
        showAuthScreen -> Color.Transparent
        showHomeSearchFilterScreen -> Color.Transparent
        showHomeSearchResultsScreen -> Color.Transparent
        showFavoriteFieldsScreen -> Color.Transparent
        showBookingScreen || showBookingPaymentScreen || showBookingConfirmationScreen -> Color.Transparent
        showBookingDetailScreen || showConversationScreen || showNotificationDetailScreen -> MaterialTheme.colorScheme.surface
        uiState.selectedTab == UserTab.Home || uiState.selectedTab == UserTab.Profile -> Color.Transparent
        else -> MaterialTheme.colorScheme.surface
    }
    val useDarkStatusBarIcons = !showAuthScreen &&
        !showHomeSearchFilterScreen &&
        !showHomeSearchResultsScreen &&
        !showFavoriteFieldsScreen &&
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
        !showHomeSearchResultsScreen &&
        !showFavoriteFieldsScreen

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

    LaunchedEffect(inboxUiState.activeBookingDetail) {
        inboxUiState.activeBookingDetail?.let { detail ->
            bookingDetailInfo = detail
        }
    }

    LaunchedEffect(inboxUiState.activeNotificationDetail) {
        inboxUiState.activeNotificationDetail?.let { detail ->
            notificationDetailInfo = detail
        }
    }

    LaunchedEffect(uiState.isAuthenticated, uiState.selectedTab) {
        if (uiState.isAuthenticated && uiState.selectedTab == UserTab.Inbox) {
            inboxViewModel.refreshInbox()
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
                    userName = bookingContactName,
                    userPhone = bookingContactPhone,
                    incomingMomoPaymentReturn = incomingMomoPaymentReturn,
                    onMomoPaymentReturnConsumed = onMomoPaymentReturnConsumed,
                    onBackClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = true
                    },
                    onConfirmBookingClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = false
                        showBookingScreen = false
                        bookingConfirmationData = null
                        selectedBookingField = null
                    },
                    onViewCancelledBookingClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = false
                        showBookingScreen = false
                        bookingConfirmationData = null
                        selectedBookingField = null
                        closeHomeOverlayFlows()
                        resolvedUserViewModel.onTabSelected(UserTab.Inbox)
                    },
                    onReturnHomeClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = false
                        showBookingScreen = false
                        bookingConfirmationData = null
                        selectedBookingField = null
                        closeHomeOverlayFlows()
                        resolvedUserViewModel.onTabSelected(UserTab.Home)
                    }
                )
            } else if (showBookingConfirmationScreen && bookingConfirmationData != null) {
                BookingConfirmationScreen(
                    confirmationData = bookingConfirmationData!!,
                    userName = bookingContactName,
                    userPhone = bookingContactPhone,
                    isLoggedIn = uiState.isAuthenticated,
                    onBackClick = {
                        showBookingConfirmationScreen = false
                        showBookingScreen = true
                    },
                    onConfirmPaymentClick = { name, phone ->
                        bookingContactName = name
                        bookingContactPhone = phone
                        showBookingConfirmationScreen = false
                        showBookingPaymentScreen = true
                    }
                )
            } else if (showBookingDetailScreen && bookingDetailInfo != null) {
                BookingDetailScreen(
                    info = bookingDetailInfo!!,
                    onBackClick = {
                        showBookingDetailScreen = false
                        inboxViewModel.clearActiveBookingDetail()
                    },
                    onOpenChat = { info ->
                        conversationInfo = info
                        showBookingDetailScreen = false
                        showConversationScreen = true
                    }
                )
            } else if (showConversationScreen && conversationInfo != null) {
                LaunchedEffect(conversationInfo?.conversationId) {
                    conversationInfo?.let { inboxViewModel.loadConversation(it) }
                }
                ConversationScreen(
                    info = conversationInfo!!,
                    messages = inboxUiState.conversationMessages,
                    draft = inboxUiState.draftMessage,
                    isSending = inboxUiState.isSendingMessage,
                    isLoading = inboxUiState.isLoadingConversation,
                    errorMessage = inboxUiState.conversationError,
                    onDraftChange = inboxViewModel::onDraftChanged,
                    onSend = inboxViewModel::sendMessage,
                    onRetry = { conversationInfo?.let { inboxViewModel.loadConversation(it) } },
                    onBackClick = {
                        showConversationScreen = false
                        inboxViewModel.clearConversationState()
                    }
                )
            } else if (showNotificationDetailScreen && notificationDetailInfo != null) {
                NotificationDetailScreen(
                    info = notificationDetailInfo!!,
                    onBackClick = {
                        showNotificationDetailScreen = false
                        inboxViewModel.clearActiveNotificationDetail()
                    },
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
                    sessionKey = bookingSessionKey,
                    onBackClick = {
                        showBookingScreen = false
                        bookingConfirmationData = null
                        selectedBookingField = null
                    },
                    onNextClick = { confirmationData ->
                        val selectedField = selectedBookingField
                        bookingConfirmationData = confirmationData.copy(
                            fieldId = selectedField?.fieldId,
                            fieldName = selectedField?.name.orEmpty(),
                            fieldAddress = selectedField?.location.orEmpty()
                        )
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
                            favoriteFields = uiState.favoriteFields,
                            isLoading = uiState.isHomeLoading,
                            title = appContext.getString(R.string.home_search_results_title),
                            emptyTitle = appContext.getString(R.string.home_search_results_empty_title),
                            emptyBody = appContext.getString(R.string.home_search_results_empty_body),
                            onBackClick = closeHomeSearchResultsFlow,
                            onFilterClick = {
                                showHomeSearchFilterScreen = true
                            },
                            onBookFieldClick = startBookingFlow,
                            onFavoriteFieldClick = toggleFavoriteField,
                            onShareFieldClick = shareField
                        )
                    } else if (showFavoriteFieldsScreen) {
                        HomeSearchResultsScreen(
                            padding = padding,
                            fields = uiState.favoriteFields,
                            favoriteFields = uiState.favoriteFields,
                            isLoading = false,
                            title = appContext.getString(R.string.favorite_title),
                            emptyTitle = "Chua co san yeu thich",
                            emptyBody = "Nhan tim o the san de luu san vao danh sach nay.",
                            onBackClick = closeFavoriteFieldsFlow,
                            onFilterClick = {},
                            onBookFieldClick = startBookingFlow,
                            onFavoriteFieldClick = toggleFavoriteField,
                            onShareFieldClick = shareField,
                            showFilterButton = false
                        )
                    } else {
                        UserHomeScreen(
                            padding = padding,
                            fields = uiState.homeFields,
                            favoriteFields = uiState.favoriteFields,
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
                            onLoginClick = openAuthFlow,
                            onRegisterClick = {
                                resolvedUserViewModel.clearAuthError()
                                showRegister = true
                                showAuthScreen = true
                            },
                            onFavoriteHeaderClick = {
                                if (uiState.isAuthenticated) {
                                    showFavoriteFieldsScreen = true
                                } else {
                                    openAuthFlow()
                                }
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
                            onBookFieldClick = startBookingFlow,
                            onFavoriteFieldClick = toggleFavoriteField,
                            onShareFieldClick = shareField,
                            deepLinkFieldIdToOpen = pendingDeepLinkFieldId,
                            onDeepLinkFieldConsumed = {
                                pendingDeepLinkFieldId = null
                                onDeepLinkConsumed()
                            }
                        )
                    }
                    UserTab.Map -> UserMapScreen(
                        padding = padding,
                        sportCategories = uiState.sportCategories,
                        nearby = uiState.nearbyFields,
                        favoriteFields = uiState.favoriteFields,
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
                        onBookFieldClick = startBookingFlow,
                        onFavoriteFieldClick = toggleFavoriteField,
                        onShareFieldClick = shareField
                    )
                    UserTab.Inbox -> InboxScreen(
                        padding = padding,
                        sections = inboxUiState.sections,
                        isLoading = inboxUiState.isLoadingInbox,
                        errorMessage = inboxUiState.inboxError,
                        onRefresh = inboxViewModel::refreshInbox,
                        onMarkAllRead = inboxViewModel::markAllRead,
                        onNotificationOpened = inboxViewModel::markNotificationRead,
                        onBookingSelected = { info ->
                            inboxViewModel.loadBookingDetail(
                                bookingId = info.bookingId,
                                notificationId = info.notificationId
                            )
                            bookingDetailInfo = info
                            showBookingDetailScreen = true
                        },
                        onMessageSelected = { info ->
                            inboxViewModel.loadConversation(info)
                            conversationInfo = info
                            showConversationScreen = true
                        },
                        onNotificationSelected = { item ->
                            inboxViewModel.loadNotificationDetail(item.id, item.detailInfo)
                            notificationDetailInfo = item.detailInfo
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

            if (fieldToShare != null) {
                FieldShareSheet(
                    field = fieldToShare!!,
                    shareUrl = selectedFieldShareUrl ?: "",
                    onDismiss = { fieldToShare = null },
                    onCopyLink = {
                        val link = selectedFieldShareUrl
                        if (link.isNullOrBlank()) {
                            Toast.makeText(context, context.getString(R.string.share_link_invalid), Toast.LENGTH_SHORT).show()
                        } else {
                            clipboardManager.setText(AnnotatedString(link))
                            Toast.makeText(context, context.getString(R.string.share_link_copied), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShareNow = performShareNow,
                    onOpenLink = {
                        val link = selectedFieldShareUrl
                        if (link.isNullOrBlank()) {
                            Toast.makeText(context, context.getString(R.string.share_link_invalid), Toast.LENGTH_SHORT).show()
                        } else {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                            } catch (_: Exception) {
                                Toast.makeText(context, context.getString(R.string.share_link_open_fail), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}


