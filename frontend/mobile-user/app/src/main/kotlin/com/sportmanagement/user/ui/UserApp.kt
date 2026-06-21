package com.sportmanagement.user.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
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
import com.sportmanagement.user.ui.components.AppRotatingLoadingIndicator
import com.sportmanagement.user.ui.components.UserBottomBar
import com.sportmanagement.user.ui.components.chatbot.ChatbotOverlay
import com.sportmanagement.user.ui.components.share.FieldShareSheet
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.screens.BookingConfirmationScreen
import com.sportmanagement.user.ui.screens.BookingHistoryScreen
import com.sportmanagement.user.ui.screens.HomeSearchFilterScreen
import com.sportmanagement.user.ui.screens.BookingPaymentScreen
import com.sportmanagement.user.ui.screens.BookingScheduleScreen
import com.sportmanagement.user.ui.screens.BookingDetailScreen
import com.sportmanagement.user.ui.screens.ConversationScreen
import com.sportmanagement.user.ui.screens.ConversationInfo
import com.sportmanagement.user.ui.screens.NotificationDetailScreen
import com.sportmanagement.user.ui.screens.NotificationDetailInfo
import com.sportmanagement.user.ui.screens.LoginScreen
import com.sportmanagement.user.ui.screens.RegisterScreen
import com.sportmanagement.user.ui.screens.InboxScreen
import com.sportmanagement.user.ui.screens.InboxCategoryType
import com.sportmanagement.user.ui.screens.ActivityInboxTab
import com.sportmanagement.user.ui.screens.HomeSearchResultsScreen
import com.sportmanagement.user.ui.screens.UserHomeScreen
import com.sportmanagement.user.ui.screens.UserMapScreen
import com.sportmanagement.user.ui.screens.UserProfileScreen
import com.sportmanagement.user.ui.screens.SettingsScreen
import com.sportmanagement.user.ui.screens.SupportFaqScreen
import com.sportmanagement.user.ui.screens.FindOpponentDraft
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
    onMomoPaymentReturnConsumed: () -> Unit = {},
    showReviewDemoUi: Boolean = false
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
    var showBookingHistoryScreen by rememberSaveable { mutableStateOf(false) }
    var showSupportFaqScreen by rememberSaveable { mutableStateOf(false) }
    var showSettingsScreen by rememberSaveable { mutableStateOf(false) }
    var showBookingDetailScreen by rememberSaveable { mutableStateOf(false) }
    var showConversationScreen by rememberSaveable { mutableStateOf(false) }
    var showNotificationDetailScreen by rememberSaveable { mutableStateOf(false) }
    var selectedInboxCategory by rememberSaveable { mutableStateOf<InboxCategoryType?>(null) }
    var selectedInboxActivityTab by rememberSaveable { mutableStateOf(ActivityInboxTab.Match) }
    var bookingContactName by rememberSaveable { mutableStateOf("") }
    var bookingContactPhone by rememberSaveable { mutableStateOf("") }
    var bookingNote by rememberSaveable { mutableStateOf("") }
    var bookingConfirmationData by remember { mutableStateOf<BookingConfirmationData?>(null) }
    var bookingFindOpponentDraft by remember { mutableStateOf<FindOpponentDraft?>(null) }
    var selectedBookingField by remember { mutableStateOf<UserField?>(null) }
    var conversationInfo by remember { mutableStateOf<ConversationInfo?>(null) }
    var notificationDetailInfo by remember { mutableStateOf<NotificationDetailInfo?>(null) }
    var fieldToShare by remember { mutableStateOf<UserField?>(null) }
    var pendingDeepLinkFieldId by rememberSaveable { mutableStateOf<Int?>(null) }
    var skipNextInboxTabRefresh by remember { mutableStateOf(false) }
    var hasPrefetchedInboxThisSession by rememberSaveable { mutableStateOf(false) }

    val closeHomeSearchResultsFlow = {
        showHomeSearchResultsScreen = false
        resolvedUserViewModel.resetHomeSearchCriteria()
    }
    val closeFavoriteFieldsFlow = {
        showFavoriteFieldsScreen = false
    }
    val closeAccountFlows = {
        showHomeSearchFilterScreen = false
        showHomeSearchResultsScreen = false
        showFavoriteFieldsScreen = false
        showBookingHistoryScreen = false
        showSupportFaqScreen = false
        showSettingsScreen = false
        showBookingScreen = false
        showBookingConfirmationScreen = false
        showBookingPaymentScreen = false
        showBookingDetailScreen = false
        showConversationScreen = false
        showNotificationDetailScreen = false
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
    val changePasswordAction: suspend (String, String) -> Unit = { currentPassword, newPassword ->
        resolvedUserViewModel.changePassword(
            currentPassword = currentPassword,
            newPassword = newPassword
        )
    }
    val startBookingFlow: (UserField) -> Unit = { field ->
        selectedBookingField = field
        selectedFieldId = field.fieldId
        bookingSessionKey += 1
        bookingContactName = uiState.profile.name
        bookingContactPhone = uiState.profile.phone
        bookingNote = ""
        bookingFindOpponentDraft = null
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

    val demoReviewBooking = remember {
        com.sportmanagement.user.ui.screens.BookingInfo(
            fieldName = "Sân bóng Bắc Từ Liêm Arena",
            timeRange = "18:00 - 20:00",
            dateLabel = "09/06/2026",
            bookingCode = "#B20260609",
            statusLabel = "Đã hoàn thành",
            statusCode = "expired",
            address = "Cầu Diễn, Bắc Từ Liêm, Hà Nội",
            paymentMethod = "MoMo",
            totalAmount = "600.000 VND",
            transactionId = "MOMO20260609001",
            orderId = "ORDER20260609001",
            checkInCode = "Q7K2M9A1",
            shareUrl = "https://sport-management.vn/l/booking/demo",
            customerName = "Hoàng Nguyễn Văn",
            customerPhone = "0901234567",
            ownerPhone = "0987654321",
            ownerNote = "Vui lòng đến trước 10 phút để check-in.",
            fieldId = 101,
            bookingId = 20260609,
            notificationId = 998,
            canReview = true,
            reviewSubmitted = false
        )
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
        showBookingHistoryScreen = false
        showSupportFaqScreen = false
        showSettingsScreen = false
        showBookingDetailScreen = false
        showConversationScreen = false
        showNotificationDetailScreen = false
        showBookingScreen = false
        showBookingConfirmationScreen = false
        showBookingPaymentScreen = false
        selectedFieldId = null
        bookingConfirmationData = null
        bookingFindOpponentDraft = null
        selectedBookingField = null
        resolvedUserViewModel.onTabSelected(UserTab.Home)
    }

    val statusBarColor = when {
        showAuthScreen -> Color.Transparent
        showHomeSearchFilterScreen -> Color.Transparent
        showHomeSearchResultsScreen -> Color.Transparent
        showFavoriteFieldsScreen -> Color.Transparent
        showBookingHistoryScreen -> Color.Transparent
        showSupportFaqScreen -> Color.Transparent
        showSettingsScreen -> Color.Transparent
        showBookingScreen || showBookingPaymentScreen || showBookingConfirmationScreen -> Color.Transparent
        showConversationScreen -> Color.White
        showBookingDetailScreen || showNotificationDetailScreen -> MaterialTheme.colorScheme.surface
        uiState.selectedTab == UserTab.Home || uiState.selectedTab == UserTab.Map || uiState.selectedTab == UserTab.Inbox || uiState.selectedTab == UserTab.Profile -> Color.Transparent
        else -> MaterialTheme.colorScheme.surface
    }
    val useDarkStatusBarIcons = !showAuthScreen &&
        !showHomeSearchFilterScreen &&
        !showHomeSearchResultsScreen &&
        !showFavoriteFieldsScreen &&
        !showBookingHistoryScreen &&
        !showSupportFaqScreen &&
        !showSettingsScreen &&
        !showBookingScreen &&
        !showBookingConfirmationScreen &&
        !showBookingPaymentScreen &&
        !showBookingDetailScreen &&
        !showNotificationDetailScreen &&
        !showBookingHistoryScreen &&
        uiState.selectedTab != UserTab.Home &&
        uiState.selectedTab != UserTab.Map &&
        uiState.selectedTab != UserTab.Inbox &&
        uiState.selectedTab != UserTab.Profile
    val shouldShowBottomBar = !showBookingScreen &&
        !showBookingConfirmationScreen &&
        !showBookingPaymentScreen &&
        !showBookingDetailScreen &&
        !showConversationScreen &&
        !showNotificationDetailScreen &&
        !showBookingHistoryScreen &&
        !showSupportFaqScreen &&
        !showSettingsScreen &&
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

    LaunchedEffect(inboxUiState.activeNotificationDetail) {
        inboxUiState.activeNotificationDetail?.let { detail ->
            notificationDetailInfo = detail
        }
    }

    LaunchedEffect(uiState.isAuthenticated, uiState.selectedTab) {
        if (uiState.isAuthenticated && uiState.selectedTab == UserTab.Inbox) {
            if (skipNextInboxTabRefresh) {
                skipNextInboxTabRefresh = false
            } else {
                inboxViewModel.refreshInbox(
                    silent = inboxUiState.sections.isNotEmpty()
                )
            }
        } else {
            skipNextInboxTabRefresh = false
        }
    }

    LaunchedEffect(uiState.isAuthenticated, uiState.isHomeLoading, shouldShowBottomBar) {
        if (!uiState.isAuthenticated) {
            hasPrefetchedInboxThisSession = false
            return@LaunchedEffect
        }
        if (!shouldShowBottomBar || uiState.isHomeLoading || hasPrefetchedInboxThisSession) {
            return@LaunchedEffect
        }
        hasPrefetchedInboxThisSession = true
        inboxViewModel.refreshInbox(silent = true)
    }

    val handleTabSelected: (UserTab) -> Unit = { tab ->
        if (tab == uiState.selectedTab) {
            Unit
        } else {
            if (uiState.isAuthenticated && tab == UserTab.Inbox) {
                skipNextInboxTabRefresh = true
                inboxViewModel.refreshInbox(
                    silent = inboxUiState.sections.isNotEmpty()
                )
            }
            resolvedUserViewModel.onTabSelected(tab)
        }
    }

    val inboxUnreadCount = inboxUiState.sections.sumOf { section ->
        section.items.sumOf { item ->
            when {
                item.badgeCount > 0 -> item.badgeCount
                item.unread -> 1
                else -> 0
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                UserBottomBar(
                    selectedTab = uiState.selectedTab,
                    inboxUnreadCount = inboxUnreadCount,
                    onTabSelected = handleTabSelected
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
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
                    bookingNote = bookingNote,
                    findOpponentDraft = bookingFindOpponentDraft,
                    incomingMomoPaymentReturn = incomingMomoPaymentReturn,
                    onMomoPaymentReturnConsumed = onMomoPaymentReturnConsumed,
                    onBackClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = true
                    },
                    onViewInvoiceClick = { invoiceInfo ->
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = false
                        showBookingScreen = false
                        closeHomeOverlayFlows()
                        invoiceInfo.bookingId?.let { bookingId ->
                            showBookingDetailScreen = true
                            inboxViewModel.loadBookingDetail(bookingId, null)
                        }
                    },
                    onViewCancelledBookingClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = false
                        showBookingScreen = false
                        bookingConfirmationData = null
                        bookingFindOpponentDraft = null
                        selectedBookingField = null
                        closeHomeOverlayFlows()
                        resolvedUserViewModel.onTabSelected(UserTab.Inbox)
                    },
                    onReturnHomeClick = {
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = false
                        showBookingScreen = false
                        bookingConfirmationData = null
                        bookingFindOpponentDraft = null
                        selectedBookingField = null
                        closeHomeOverlayFlows()
                        resolvedUserViewModel.onTabSelected(UserTab.Home)
                    },
                    onPaymentConfirmed = {
                        inboxViewModel.refreshInbox(withBookingFollowUp = true)
                    }
                )
            } else if (showBookingConfirmationScreen && bookingConfirmationData != null) {
                BookingConfirmationScreen(
                    confirmationData = bookingConfirmationData!!,
                    userName = bookingContactName,
                    userPhone = bookingContactPhone,
                    isLoggedIn = uiState.isAuthenticated,
                    findOpponentDraft = bookingFindOpponentDraft,
                    onBackClick = {
                        showBookingConfirmationScreen = false
                        showBookingScreen = true
                    },
                    onConfirmPaymentClick = { name, phone, note ->
                        bookingContactName = name
                        bookingContactPhone = phone
                        bookingNote = note
                        showBookingConfirmationScreen = false
                        showBookingPaymentScreen = true
                    }
                )
            } else if (showReviewDemoUi) {
                BookingDetailScreen(
                    info = demoReviewBooking,
                    isSubmittingReview = false,
                    reviewSubmissionError = null,
                    reviewSubmissionSuccessMessage = null,
                    processingMatchRequestId = null,
                    matchRequestActionError = null,
                    matchRequestActionSuccessMessage = null,
                    onBackClick = {},
                    onSubmitReview = { _, _, _ -> },
                    onReviewFeedbackConsumed = {},
                    onAcceptMatchRequest = {},
                    onRejectMatchRequest = {},
                    onMatchRequestFeedbackConsumed = {},
                    onOpenChat = {},
                    autoOpenReviewSheet = true
                )
            } else if (showBookingDetailScreen) {
                when {
                    inboxUiState.activeBookingDetail != null -> {
                        BookingDetailScreen(
                            info = inboxUiState.activeBookingDetail!!,
                            isSubmittingReview = inboxUiState.isSubmittingReview,
                            reviewSubmissionError = inboxUiState.reviewSubmissionError,
                            reviewSubmissionSuccessMessage = inboxUiState.reviewSubmissionSuccessMessage,
                            processingMatchRequestId = inboxUiState.processingMatchRequestId,
                            matchRequestActionError = inboxUiState.matchRequestActionError,
                            matchRequestActionSuccessMessage = inboxUiState.matchRequestActionSuccessMessage,
                            autoOpenReviewSheet = false,
                            onBackClick = {
                                showBookingDetailScreen = false
                                inboxViewModel.clearActiveBookingDetail()
                            },
                            onSubmitReview = { rating, comment, imageUri ->
                                inboxViewModel.submitReview(rating, comment, imageUri)
                            },
                            onReviewFeedbackConsumed = inboxViewModel::clearReviewSubmissionFeedback,
                            onAcceptMatchRequest = { inboxViewModel.respondToMatchRequest(it, accept = true) },
                            onRejectMatchRequest = { inboxViewModel.respondToMatchRequest(it, accept = false) },
                            onMatchRequestFeedbackConsumed = inboxViewModel::clearMatchRequestActionFeedback,
                            onOpenChat = { info ->
                                conversationInfo = info
                                showBookingDetailScreen = false
                                showConversationScreen = true
                            }
                        )
                    }
                    inboxUiState.isLoadingBookingDetail -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            AppRotatingLoadingIndicator(
                                label = null
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(padding)
                            ) {
                                Text(
                                    text = inboxUiState.bookingDetailError ?: "Không tải được chi tiết đặt sân.",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextButton(
                                    onClick = {
                                        showBookingDetailScreen = false
                                        inboxViewModel.clearActiveBookingDetail()
                                    }
                                ) {
                                    Text(text = "Quay lại")
                                }
                            }
                        }
                    }
                }
            } else if (showBookingHistoryScreen) {
                BookingHistoryScreen(
                    padding = padding,
                    sections = inboxUiState.sections,
                    isLoading = inboxUiState.isLoadingInbox,
                    errorMessage = inboxUiState.inboxError,
                    onBackClick = {
                        showBookingHistoryScreen = false
                    },
                    onRefresh = inboxViewModel::refreshInbox,
                    onBookingSelected = { bookingId, notificationId ->
                        showBookingHistoryScreen = false
                        showBookingDetailScreen = true
                        inboxViewModel.loadBookingDetail(bookingId, notificationId)
                    }
                )
            } else if (showSupportFaqScreen) {
                SupportFaqScreen(
                    padding = padding,
                    onBackClick = {
                        showSupportFaqScreen = false
                    }
                )
            } else if (showSettingsScreen) {
                SettingsScreen(
                    padding = padding,
                    onBackClick = {
                        showSettingsScreen = false
                    },
                    onChangePassword = changePasswordAction
                )
            } else if (showFavoriteFieldsScreen) {
                HomeSearchResultsScreen(
                    padding = padding,
                    fields = uiState.favoriteFields,
                    favoriteFields = uiState.favoriteFields,
                    fieldReviewStatsByFieldId = uiState.fieldReviewStatsByFieldId,
                    fieldReviewsByFieldId = uiState.fieldReviewsByFieldId,
                    loadingFieldReviewIds = uiState.loadingFieldReviewIds,
                    fieldDetailDataByFieldId = uiState.fieldDetailsByFieldId,
                    isLoading = false,
                    title = appContext.getString(R.string.favorite_title),
                    emptyTitle = "Chua co san yeu thich",
                    emptyBody = "Nhan tim o the san de luu san vao danh sach nay.",
                    onBackClick = closeFavoriteFieldsFlow,
                    onFilterClick = {},
                    onBookFieldClick = startBookingFlow,
                    onFavoriteFieldClick = toggleFavoriteField,
                    onShareFieldClick = shareField,
                    onFieldDetailOpened = { fieldId ->
                        resolvedUserViewModel.loadFieldReviewData(fieldId, force = true)
                    },
                    showFilterButton = false
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
            } else if (showNotificationDetailScreen) {
                if (inboxUiState.isLoadingNotificationDetail) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        AppRotatingLoadingIndicator(
                            label = null
                        )
                    }
                } else if (notificationDetailInfo != null) {
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
                        },
                        processingMatchRequestId = inboxUiState.processingMatchRequestId,
                        matchRequestActionError = inboxUiState.matchRequestActionError,
                        matchRequestActionSuccessMessage = inboxUiState.matchRequestActionSuccessMessage,
                        onAcceptMatchRequest = { matchRequestId ->
                            inboxViewModel.respondToNotificationMatchRequest(matchRequestId, accept = true)
                        },
                        onRejectMatchRequest = { matchRequestId ->
                            inboxViewModel.respondToNotificationMatchRequest(matchRequestId, accept = false)
                        }
                    )
                }
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
                    onNextClick = { confirmationData, findOpponentDraft ->
                        val selectedField = selectedBookingField
                        bookingConfirmationData = confirmationData.copy(
                            fieldId = selectedField?.fieldId,
                            fieldName = selectedField?.name.orEmpty(),
                            fieldAddress = selectedField?.location.orEmpty()
                        )
                        bookingFindOpponentDraft = findOpponentDraft
                        showBookingScreen = false
                        showBookingPaymentScreen = false
                        showBookingConfirmationScreen = true
                    }
                )
            } else {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = {
                        val isForward = targetState.ordinal > initialState.ordinal
                        val enterOffset: (Int) -> Int = { fullWidth ->
                            if (isForward) fullWidth else -fullWidth
                        }
                        val exitOffset: (Int) -> Int = { fullWidth ->
                            if (isForward) -fullWidth / 4 else fullWidth / 4
                        }
                        (slideInHorizontally(
                            initialOffsetX = enterOffset,
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 320)
                        ) + fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 220)
                        )).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = exitOffset,
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 280)
                            ) + fadeOut(
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 180)
                            )
                        )
                    },
                    label = "user_tab_transition"
                ) { selectedTab ->
                    when (selectedTab) {
                            UserTab.Home -> if (showHomeSearchResultsScreen) {
                                HomeSearchResultsScreen(
                                    padding = padding,
                                    fields = uiState.homeFields,
                                    favoriteFields = uiState.favoriteFields,
                                    fieldReviewStatsByFieldId = uiState.fieldReviewStatsByFieldId,
                                    fieldReviewsByFieldId = uiState.fieldReviewsByFieldId,
                                    loadingFieldReviewIds = uiState.loadingFieldReviewIds,
                                    fieldDetailDataByFieldId = uiState.fieldDetailsByFieldId,
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
                                    onShareFieldClick = shareField,
                                    onFieldDetailOpened = { fieldId ->
                                        resolvedUserViewModel.loadFieldReviewData(fieldId, force = true)
                                    }
                                )
                            } else if (showFavoriteFieldsScreen) {
                                HomeSearchResultsScreen(
                                    padding = padding,
                                    fields = uiState.favoriteFields,
                                    favoriteFields = uiState.favoriteFields,
                                    fieldReviewStatsByFieldId = uiState.fieldReviewStatsByFieldId,
                                    fieldReviewsByFieldId = uiState.fieldReviewsByFieldId,
                                    loadingFieldReviewIds = uiState.loadingFieldReviewIds,
                                    fieldDetailDataByFieldId = uiState.fieldDetailsByFieldId,
                                    isLoading = false,
                                    title = appContext.getString(R.string.favorite_title),
                                    emptyTitle = "Chua co san yeu thich",
                                    emptyBody = "Nhan tim o the san de luu san vao danh sach nay.",
                                    onBackClick = closeFavoriteFieldsFlow,
                                    onFilterClick = {},
                                    onBookFieldClick = startBookingFlow,
                                    onFavoriteFieldClick = toggleFavoriteField,
                                    onShareFieldClick = shareField,
                                    onFieldDetailOpened = { fieldId ->
                                        resolvedUserViewModel.loadFieldReviewData(fieldId, force = true)
                                    },
                                    showFilterButton = false
                                )
                            } else {
                                UserHomeScreen(
                                    padding = padding,
                                    fields = uiState.homeFields,
                                    favoriteFields = uiState.favoriteFields,
                                    fieldReviewStatsByFieldId = uiState.fieldReviewStatsByFieldId,
                                    fieldReviewsByFieldId = uiState.fieldReviewsByFieldId,
                                    loadingFieldReviewIds = uiState.loadingFieldReviewIds,
                                    fieldDetailDataByFieldId = uiState.fieldDetailsByFieldId,
                                    sportCategories = uiState.sportCategories,
                                    userName = uiState.profile.name,
                                    userAvatarUrl = uiState.profile.avatarUrl,
                                    isLoggedIn = uiState.isAuthenticated,
                                    isInitialLoading = uiState.isHomeLoading,
                                    isLoadingMore = uiState.isHomeLoadingMore,
                                    hasMoreData = uiState.hasMoreHomeFields,
                                    searchResults = uiState.fieldSearchResults,
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
                                    onFieldDetailOpened = { fieldId ->
                                        resolvedUserViewModel.loadFieldReviewData(fieldId, force = true)
                                    },
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
                                fieldReviewStatsByFieldId = uiState.fieldReviewStatsByFieldId,
                                fieldReviewsByFieldId = uiState.fieldReviewsByFieldId,
                                loadingFieldReviewIds = uiState.loadingFieldReviewIds,
                                fieldDetailDataByFieldId = uiState.fieldDetailsByFieldId,
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
                                onShareFieldClick = shareField,
                                onFieldDetailOpened = { fieldId ->
                                    resolvedUserViewModel.loadFieldReviewData(fieldId, force = true)
                                }
                            )
                            UserTab.Inbox -> InboxScreen(
                                padding = padding,
                                sections = inboxUiState.sections,
                                isLoading = inboxUiState.isLoadingInbox,
                                errorMessage = inboxUiState.inboxError,
                                selectedCategory = selectedInboxCategory,
                                selectedActivityTab = selectedInboxActivityTab,
                                onSelectedCategoryChange = { selectedInboxCategory = it },
                                onSelectedActivityTabChange = { selectedInboxActivityTab = it },
                                onRefresh = { inboxViewModel.refreshInbox() },
                                onMarkAllRead = inboxViewModel::markAllRead,
                                onNotificationOpened = inboxViewModel::markNotificationRead,
                                onBookingSelected = { info ->
                                    showBookingDetailScreen = true
                                    inboxViewModel.loadBookingDetail(
                                        bookingId = info.bookingId,
                                        notificationId = info.notificationId
                                    )
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
                                onBookingHistoryClick = {
                                    closeAccountFlows()
                                    if (uiState.isAuthenticated) {
                                        showBookingHistoryScreen = true
                                    } else {
                                        openAuthFlow()
                                    }
                                },
                                onFavoriteClick = {
                                    closeAccountFlows()
                                    if (uiState.isAuthenticated) {
                                        showFavoriteFieldsScreen = true
                                    } else {
                                        openAuthFlow()
                                    }
                                },
                                onSupportClick = {
                                    closeAccountFlows()
                                    showSupportFaqScreen = true
                                },
                                onSettingsClick = {
                                    closeAccountFlows()
                                    showSettingsScreen = true
                                },
                                onProfileUpdate = { updatedProfile ->
                                    resolvedUserViewModel.updateProfile(updatedProfile)
                                }
                            )
                            }
                }
            }

            ChatbotOverlayHost(
                chatbotViewModel = chatbotViewModel,
                contentPadding = padding
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

private sealed interface UserSurfaceScreen {
    val order: Int
    val stateKey: String

    data object Home : UserSurfaceScreen {
        override val order: Int = 0
        override val stateKey: String = "home"
    }

    data object Map : UserSurfaceScreen {
        override val order: Int = 1
        override val stateKey: String = "map"
    }

    data object Inbox : UserSurfaceScreen {
        override val order: Int = 2
        override val stateKey: String = "inbox"
    }

    data object Profile : UserSurfaceScreen {
        override val order: Int = 3
        override val stateKey: String = "profile"
    }

    data object HomeSearchResults : UserSurfaceScreen {
        override val order: Int = 4
        override val stateKey: String = "home_search_results"
    }

    data object FavoriteFields : UserSurfaceScreen {
        override val order: Int = 5
        override val stateKey: String = "favorite_fields"
    }

    data object HomeSearchFilter : UserSurfaceScreen {
        override val order: Int = 6
        override val stateKey: String = "home_search_filter"
    }

    data object BookingSchedule : UserSurfaceScreen {
        override val order: Int = 7
        override val stateKey: String = "booking_schedule"
    }

    data object BookingConfirmation : UserSurfaceScreen {
        override val order: Int = 8
        override val stateKey: String = "booking_confirmation"
    }

    data object BookingPayment : UserSurfaceScreen {
        override val order: Int = 9
        override val stateKey: String = "booking_payment"
    }

    data object BookingHistory : UserSurfaceScreen {
        override val order: Int = 10
        override val stateKey: String = "booking_history"
    }

    data object SupportFaq : UserSurfaceScreen {
        override val order: Int = 11
        override val stateKey: String = "support_faq"
    }

    data object Settings : UserSurfaceScreen {
        override val order: Int = 12
        override val stateKey: String = "settings"
    }

    data object BookingDetailLoading : UserSurfaceScreen {
        override val order: Int = 13
        override val stateKey: String = "booking_detail_loading"
    }

    data object BookingDetail : UserSurfaceScreen {
        override val order: Int = 14
        override val stateKey: String = "booking_detail"
    }

    data object Conversation : UserSurfaceScreen {
        override val order: Int = 15
        override val stateKey: String = "conversation"
    }

    data object NotificationDetail : UserSurfaceScreen {
        override val order: Int = 16
        override val stateKey: String = "notification_detail"
    }

    data object AuthLogin : UserSurfaceScreen {
        override val order: Int = 17
        override val stateKey: String = "auth_login"
    }

    data object AuthRegister : UserSurfaceScreen {
        override val order: Int = 18
        override val stateKey: String = "auth_register"
    }
}

@Composable
private fun ChatbotOverlayHost(
    chatbotViewModel: ChatbotViewModel,
    contentPadding: PaddingValues
) {
    val chatbotUiState by chatbotViewModel.uiState.collectAsState()

    ChatbotOverlay(
        uiState = chatbotUiState,
        contentPadding = contentPadding,
        onToggleWindow = chatbotViewModel::toggleWindow,
        onCloseWindow = chatbotViewModel::closeWindow,
        onDraftChanged = chatbotViewModel::onDraftMessageChange,
        onSendMessage = chatbotViewModel::sendDraftMessage,
        onRetryMessage = chatbotViewModel::retryMessage,
        onDismissError = chatbotViewModel::dismissError,
        onButtonAnchorChanged = chatbotViewModel::onButtonAnchorChanged
    )
}


