package com.sportmanagement.user.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sportmanagement.user.R
import com.sportmanagement.user.data.remote.api.ConversationListItemDto
import com.sportmanagement.user.data.remote.api.BookingDetailDto
import com.sportmanagement.user.data.remote.api.InboxApi
import com.sportmanagement.user.data.remote.api.InboxItemDto
import com.sportmanagement.user.data.remote.api.NotificationDto
import com.sportmanagement.user.ui.screens.BookingMatchPostInfo
import com.sportmanagement.user.ui.screens.BookingMatchRequestInfo
import com.sportmanagement.user.ui.screens.BookingInfo
import com.sportmanagement.user.ui.screens.ConversationInfo
import com.sportmanagement.user.ui.screens.ConversationMessageUi
import com.sportmanagement.user.ui.screens.InboxCategoryType
import com.sportmanagement.user.ui.screens.NotificationDetailInfo
import com.sportmanagement.user.ui.screens.NotificationItem
import com.sportmanagement.user.ui.screens.NotificationSectionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class InboxViewModel(
    private val appContext: Context,
    private val api: InboxApi = InboxApi()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    private val prefs = appContext.getSharedPreferences("user_repository_cache", Context.MODE_PRIVATE)
    private var inboxRefreshGeneration = 0L

    init {
        refreshInbox()
    }

    fun refreshInbox(
        silent: Boolean = false,
        withBookingFollowUp: Boolean = false
    ) {
        val token = token()
        if (token == null) {
            _uiState.value = _uiState.value.copy(
                isLoadingInbox = false,
                sections = emptyList(),
                inboxError = "Bạn chưa đăng nhập. Vui lòng đăng nhập để xem hộp thư."
            )
            return
        }

        val generation = ++inboxRefreshGeneration
        viewModelScope.launch {
            val shouldShowLoading = !silent || _uiState.value.sections.all { it.items.isEmpty() }
            _uiState.value = if (shouldShowLoading) {
                _uiState.value.copy(isLoadingInbox = true, inboxError = null)
            } else {
                _uiState.value.copy(inboxError = null)
            }

            val (inboxItems, notifications, conversations) = supervisorScope {
                val inboxDeferred = async { runCatching { api.getInbox(token) }.getOrDefault(emptyList()) }
                val notificationsDeferred = async { runCatching { api.getNotifications(token) }.getOrDefault(emptyList()) }
                val conversationsDeferred = async { runCatching { api.getConversations(token) }.getOrDefault(emptyList()) }
                Triple(
                    inboxDeferred.await(),
                    notificationsDeferred.await(),
                    conversationsDeferred.await()
                )
            }

            val sections = buildSections(inboxItems, notifications, conversations)
            if (generation != inboxRefreshGeneration) return@launch
            _uiState.value = _uiState.value.copy(
                isLoadingInbox = false,
                sections = sections,
                inboxError = if (sections.all { it.items.isEmpty() }) "Hộp thư chưa có dữ liệu." else null
            )

            val enrichedSections = runCatching { enrichBookingSections(token, sections) }.getOrDefault(sections)
            if (generation != inboxRefreshGeneration) return@launch
            if (enrichedSections != sections) {
                _uiState.value = _uiState.value.copy(sections = enrichedSections)
            }

            if (withBookingFollowUp && generation == inboxRefreshGeneration) {
                delay(2_500)
                if (generation == inboxRefreshGeneration) {
                    refreshInbox()
                }
            }
        }
    }

    fun markAllRead() {
        val token = token() ?: return
        val currentItems = _uiState.value.sections.flatMap { it.items }

        val unreadNotificationIds = currentItems
            .filter { it.category != InboxCategoryType.Message && it.unread }
            .mapNotNull { it.id }
            .distinct()

        val unreadConversationIds = currentItems
            .filter { it.category == InboxCategoryType.Message && it.unread }
            .mapNotNull { it.conversationId }
            .distinct()

        // Optimistically clear unread badges in UI first.
        val clearedSections = _uiState.value.sections.map { section ->
            section.copy(
                items = section.items.map { item ->
                    item.copy(unread = false, badgeCount = 0)
                }
            )
        }
        _uiState.value = _uiState.value.copy(sections = clearedSections)

        viewModelScope.launch {
            runCatching { api.markInboxReadAll(token) }
                .recoverCatching {
                    api.markAllNotificationsRead(token)
                    unreadNotificationIds.forEach { notificationId ->
                        runCatching { api.markNotificationRead(token, notificationId) }
                    }
                    unreadConversationIds.forEach { conversationId ->
                        runCatching { api.markConversationRead(token, conversationId) }
                            .recoverCatching {
                                // Fallback: opening thread API also marks incoming messages as read on backend.
                                api.getConversationMessages(token, conversationId)
                            }
                    }
                }
            refreshInbox()
        }
    }

    fun markNotificationRead(notificationId: Int?) {
        val token = token() ?: return
        val id = notificationId ?: return

        clearNotificationUnreadState(id)
        viewModelScope.launch {
            runCatching { api.markNotificationRead(token, id) }
            delay(1_200)
            refreshInbox()
        }
    }

    private fun clearNotificationUnreadState(notificationId: Int?) {
        if (notificationId == null) return

        val clearedSections = _uiState.value.sections.map { section ->
            section.copy(
                items = section.items.map { item ->
                    if (item.matchesNotificationId(notificationId)) {
                        item.copy(unread = false, badgeCount = 0)
                    } else {
                        item
                    }
                }
            )
        }

        _uiState.value = _uiState.value.copy(sections = clearedSections)
    }

    fun loadBookingDetail(bookingId: Int?, notificationId: Int? = null) {
        val token = token() ?: return
        _uiState.value = _uiState.value.copy(
            isLoadingBookingDetail = true,
            bookingDetailError = null,
            activeBookingDetail = null,
            isSubmittingReview = false,
            reviewSubmissionError = null,
            reviewSubmissionSuccessMessage = null,
            processingMatchRequestId = null,
            matchRequestActionError = null,
            matchRequestActionSuccessMessage = null
        )
        viewModelScope.launch {
            runCatching {
                val notificationDetail = notificationId?.let { nid ->
                    api.getNotificationDetail(token, nid)
                }
                val shouldFocusVenueInfo = isBookingReminderType(notificationDetail?.type)
                val shouldFocusReview = isReviewReminderType(notificationDetail?.type)
                val resolvedBookingId =
                    bookingId ?: notificationDetail?.bookingId ?: notificationDetail?.targetId
                val resolvedNotificationId = notificationId ?: resolveBookingNotificationId(resolvedBookingId)
                clearBookingUnreadState(resolvedBookingId)
                resolvedNotificationId?.let { clearNotificationUnreadState(it) }
                resolvedNotificationId?.let { runCatching { api.markNotificationRead(token, it) } }
                resolvedBookingId?.let { runCatching { api.markBookingNotificationsRead(token, it) } }
                resolvedBookingId?.let { api.getBookingDetail(token, it) } to Pair(shouldFocusVenueInfo, shouldFocusReview)
            }
                .onSuccess { (booking, focusFlags) ->
                    if (booking == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoadingBookingDetail = false,
                            bookingDetailError = "Không tải được chi tiết đặt sân."
                        )
                        return@onSuccess
                    }
                    val (shouldFocusVenueInfo, shouldFocusReview) = focusFlags
                    val dateLabel = booking.date.ifBlank { "" }
                    val timeRange = booking.timeRange.ifBlank {
                        listOf(booking.startTime, booking.endTime)
                            .filter { it.isNotBlank() }
                            .joinToString(" - ")
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoadingBookingDetail = false,
                        bookingDetailError = null,
                        activeBookingDetail = BookingInfo(
                            fieldName = booking.fieldName,
                            timeRange = timeRange,
                            dateLabel = dateLabel,
                            bookingCode = booking.bookingCode,
                            statusLabel = booking.status,
                            statusCode = booking.statusCode,
                            address = booking.fieldAddress,
                            paymentMethod = booking.paymentMethod,
                            totalAmount = booking.totalPrice,
                            transactionId = booking.transactionId,
                            orderId = booking.orderId,
                            checkInCode = booking.checkInCode,
                            shareUrl = booking.shareUrl,
                            customerName = booking.userName,
                            customerPhone = booking.userPhone,
                            ownerPhone = booking.ownerPhone,
                            ownerNote = booking.ownerNote,
                            fieldId = booking.fieldId,
                            bookingId = booking.bookingId,
                            notificationId = null,
                            canReview = booking.canReview,
                            reviewSubmitted = booking.reviewSubmitted,
                            reviewId = booking.reviewId,
                            reviewRating = booking.reviewRating,
                            reviewComment = booking.reviewComment,
                            matchPost = booking.matchPost?.let {
                                BookingMatchPostInfo(
                                    matchPostId = it.matchPostId,
                                    ownerUserId = it.ownerUserId,
                                    ownerUsername = it.ownerUsername,
                                    teamName = it.teamName,
                                    playerCount = it.playerCount,
                                    level = it.level,
                                    levelLabel = it.levelLabel,
                                    description = it.description,
                                    status = it.status
                                )
                            },
                            matchRequests = booking.matchRequests.map {
                                BookingMatchRequestInfo(
                                    matchRequestId = it.matchRequestId,
                                    requesterUserId = it.requesterUserId,
                                    requesterUsername = it.requesterUsername,
                                    teamName = it.teamName,
                                    playerCount = it.playerCount,
                                    level = it.level,
                                    levelLabel = it.levelLabel,
                                    message = it.message,
                                    status = it.status,
                                    createdAt = it.createdAt
                                )
                            },
                            focusVenueInfo = shouldFocusVenueInfo,
                            reviewFocusOnly = shouldFocusReview
                        )
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingBookingDetail = false,
                        bookingDetailError = it.message ?: "Không tải được chi tiết đặt sân.",
                        activeBookingDetail = null
                    )
                }
        }
    }

    private fun resolveBookingNotificationId(bookingId: Int?): Int? {
        if (bookingId == null) return null

        return _uiState.value.sections
            .asSequence()
            .flatMap { section -> section.items.asSequence() }
            .firstOrNull { item ->
                item.bookingId == bookingId || item.bookingInfo?.bookingId == bookingId
            }
            ?.id
    }

    private fun clearBookingUnreadState(bookingId: Int?) {
        if (bookingId == null) return

        val clearedSections = _uiState.value.sections.map { section ->
            section.copy(
                items = section.items.map { item ->
                    if (
                        item.bookingId == bookingId || item.bookingInfo?.bookingId == bookingId
                    ) {
                        item.copy(unread = false, badgeCount = 0)
                    } else {
                        item
                    }
                }
            )
        }

        _uiState.value = _uiState.value.copy(sections = clearedSections)
    }

    fun clearActiveBookingDetail() {
        _uiState.value = _uiState.value.copy(
            activeBookingDetail = null,
            isLoadingBookingDetail = false,
            bookingDetailError = null,
            isSubmittingReview = false,
            reviewSubmissionError = null,
            reviewSubmissionSuccessMessage = null,
            processingMatchRequestId = null,
            matchRequestActionError = null,
            matchRequestActionSuccessMessage = null
        )
    }

    fun submitReview(rating: Int, comment: String, imageUri: String? = null) {
        val activeBooking = _uiState.value.activeBookingDetail ?: return
        val token = token() ?: return
        val bookingId = activeBooking.bookingId ?: return
        val fieldId = activeBooking.fieldId ?: return

        _uiState.value = _uiState.value.copy(
            isSubmittingReview = true,
            reviewSubmissionError = null,
            reviewSubmissionSuccessMessage = null
        )

        viewModelScope.launch {
            runCatching {
                val uploadedImages = imageUri
                    ?.takeIf { it.isNotBlank() }
                    ?.let { uploadReviewImage(token, it) }
                    .orEmpty()
                api.submitBookingReview(
                    token = token,
                    bookingId = bookingId,
                    fieldId = fieldId,
                    rating = rating,
                    comment = comment.trim(),
                    images = uploadedImages
                )
            }.onSuccess { review ->
                _uiState.value = _uiState.value.copy(
                    isSubmittingReview = false,
                    reviewSubmissionError = null,
                    reviewSubmissionSuccessMessage = "Đã gửi đánh giá của bạn.",
                    activeBookingDetail = _uiState.value.activeBookingDetail?.copy(
                        canReview = false,
                        reviewSubmitted = true,
                        reviewId = review.reviewId,
                        reviewRating = review.rating,
                        reviewComment = review.comment
                    )
                )
                refreshInbox(silent = true)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isSubmittingReview = false,
                    reviewSubmissionError = it.message ?: "Không thể gửi đánh giá lúc này."
                )
            }
        }
    }

    private suspend fun uploadReviewImage(token: String, imageUriString: String): List<String> = withContext(Dispatchers.IO) {
        val uri = Uri.parse(imageUriString)
        val resolver = appContext.contentResolver
        val mimeType = resolver.getType(uri)
            ?: guessMimeType(uri)
            ?: "image/jpeg"
        val imageBytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Unable to read selected review image")
        val fileName = buildReviewImageFileName(uri, mimeType)
        api.uploadReviewImage(
            token = token,
            imageBytes = imageBytes,
            fileName = fileName,
            mimeType = mimeType
        )
    }

    private fun guessMimeType(uri: Uri): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            .lowercase()
            .takeIf { it.isNotBlank() }
        return extension?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
    }

    private fun buildReviewImageFileName(uri: Uri, mimeType: String): String {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: "jpg"
        val rawName = uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.substringBefore('?')
            ?.substringBefore('#')
            ?.takeIf { it.isNotBlank() }
            ?: "review"
        val safeName = rawName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        return "${safeName}-${System.currentTimeMillis()}.$extension"
    }

    fun clearReviewSubmissionFeedback() {
        _uiState.value = _uiState.value.copy(
            reviewSubmissionError = null,
            reviewSubmissionSuccessMessage = null
        )
    }

    fun respondToMatchRequest(matchRequestId: Int, accept: Boolean) {
        val token = token() ?: return
        val activeBooking = _uiState.value.activeBookingDetail ?: return
        val bookingId = activeBooking.bookingId ?: return
        val notificationId = activeBooking.notificationId

        _uiState.value = _uiState.value.copy(
            processingMatchRequestId = matchRequestId,
            matchRequestActionError = null,
            matchRequestActionSuccessMessage = null
        )

        viewModelScope.launch {
            runCatching {
                if (accept) {
                    api.acceptMatchRequest(token, matchRequestId)
                } else {
                    api.rejectMatchRequest(token, matchRequestId)
                }
            }.onSuccess {
                val refreshedBooking = runCatching {
                    api.getBookingDetail(token, bookingId)
                }.getOrNull()

                _uiState.value = _uiState.value.copy(
                    processingMatchRequestId = null,
                    matchRequestActionError = null,
                    matchRequestActionSuccessMessage = if (accept) {
                        "Đã chấp nhận yêu cầu ghép trận."
                    } else {
                        "Đã từ chối yêu cầu ghép trận."
                    },
                    activeBookingDetail = refreshedBooking?.toBookingInfo(notificationId = notificationId)
                        ?: _uiState.value.activeBookingDetail?.applyMatchRequestUpdate(
                            matchRequestId = matchRequestId,
                            accept = accept
                        )
                )
                refreshInbox(silent = true)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    processingMatchRequestId = null,
                    matchRequestActionError = it.message ?: "Không thể xử lý yêu cầu ghép trận."
                )
            }
        }
    }

    fun respondToNotificationMatchRequest(matchRequestId: Int, accept: Boolean) {
        val token = token() ?: return
        val activeDetail = _uiState.value.activeNotificationDetail as? NotificationDetailInfo.MatchRequestNotice
            ?: return

        _uiState.value = _uiState.value.copy(
            processingMatchRequestId = matchRequestId,
            matchRequestActionError = null,
            matchRequestActionSuccessMessage = null
        )

        viewModelScope.launch {
            runCatching {
                if (accept) {
                    api.acceptMatchRequest(token, matchRequestId)
                } else {
                    api.rejectMatchRequest(token, matchRequestId)
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    processingMatchRequestId = null,
                    matchRequestActionError = null,
                    matchRequestActionSuccessMessage = if (accept) {
                        "Đã chấp nhận yêu cầu ghép trận."
                    } else {
                        "Đã từ chối yêu cầu ghép trận."
                    },
                    activeNotificationDetail = activeDetail.applyMatchRequestUpdate(accept)
                )
                refreshInbox(silent = true)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    processingMatchRequestId = null,
                    matchRequestActionError = it.message ?: "Không thể xử lý yêu cầu ghép trận."
                )
            }
        }
    }

    fun clearMatchRequestActionFeedback() {
        _uiState.value = _uiState.value.copy(
            matchRequestActionError = null,
            matchRequestActionSuccessMessage = null
        )
    }

    fun loadNotificationDetail(notificationId: Int?, fallback: NotificationDetailInfo?) {
        val token = token() ?: return
        val id = notificationId ?: run {
            _uiState.value = _uiState.value.copy(activeNotificationDetail = fallback, isLoadingNotificationDetail = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingNotificationDetail = true)
            runCatching {
                val detail = api.getNotificationDetail(token, id)
                when (detail.type.lowercase()) {
                    "upcoming_match" -> {
                        val booking = detail.bookingId?.let { bid ->
                            runCatching { api.getBookingDetail(token, bid) }.getOrNull()
                        }
                        NotificationDetailInfo.UpcomingMatch(
                            title = detail.title,
                            subtitle = detail.subtitle,
                            notificationId = detail.id,
                            fieldId = detail.fieldId,
                            bookingId = detail.bookingId,
                            fieldName = booking?.fieldName ?: detail.title,
                            address = booking?.fieldAddress ?: "",
                            timeRange = listOf(booking?.startTime, booking?.endTime)
                                .filter { !it.isNullOrBlank() }
                                .joinToString(" - "),
                            dateLabel = booking?.date ?: "",
                            bookingCode = booking?.bookingCode ?: (detail.bookingId?.let { "#B$it" } ?: ""),
                            statusLabel = booking?.status ?: "confirmed",
                            paymentMethod = booking?.paymentMethod ?: "",
                            totalAmount = booking?.totalPrice ?: "",
                            reminderText = detail.content.ifBlank { detail.subtitle },
                            phoneNumber = booking?.ownerPhone ?: "",
                            avatarRes = R.drawable.field_football
                        )
                    }
                    "match_request_received", "match_request_accepted" -> {
                        val booking = detail.bookingId?.let { bid ->
                            runCatching { api.getBookingDetail(token, bid) }.getOrNull()
                        }
                        buildMatchRequestDetail(detail, booking)
                    }
                    "promotion" -> NotificationDetailInfo.Promotion(
                        title = detail.title,
                        subtitle = detail.subtitle,
                        notificationId = detail.id,
                        promoTitle = detail.title,
                        promoSubtitle = detail.subtitle,
                        contentText = detail.content.ifBlank { detail.subtitle },
                        periodText = "",
                        conditions = emptyList()
                    )
                    "system", "system_notice", "match_request_rejected" -> NotificationDetailInfo.SystemNotice(
                        title = displayInboxTitle(detail.type, detail.title),
                        subtitle = matchNotificationSummary(detail.type, detail.title, detail.subtitle),
                        notificationId = detail.id,
                        contentText = detail.content.ifBlank { detail.subtitle },
                        features = emptyList(),
                        timeText = formatTime(detail.time)
                    )
                    else -> fallback
                }
            }.onSuccess { resolved ->
                _uiState.value = _uiState.value.copy(activeNotificationDetail = resolved, isLoadingNotificationDetail = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(activeNotificationDetail = fallback, isLoadingNotificationDetail = false)
            }
        }
    }

    fun clearActiveNotificationDetail() {
        _uiState.value = _uiState.value.copy(
            activeNotificationDetail = null,
            isLoadingNotificationDetail = false,
            processingMatchRequestId = null,
            matchRequestActionError = null,
            matchRequestActionSuccessMessage = null
        )
    }

    fun loadConversation(info: ConversationInfo) {
        val token = token() ?: return

        _uiState.value = _uiState.value.copy(currentConversation = info, conversationError = null)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingConversation = true)
            runCatching {
                val resolvedConversationId = info.conversationId ?: api.createConversation(
                    token = token,
                    fieldId = info.fieldId,
                    bookingId = info.bookingId,
                    peerUserId = info.peerUserId
                ).conversationId

                clearConversationUnreadState(resolvedConversationId)
                runCatching { api.markConversationRead(token, resolvedConversationId) }
                api.getConversationMessages(token, resolvedConversationId)
            }.onSuccess { thread ->
                _uiState.value = _uiState.value.copy(
                    isLoadingConversation = false,
                    currentConversation = info.copy(
                        conversationId = thread.conversationId,
                        fieldName = thread.fieldName.ifBlank { info.fieldName },
                        phoneNumber = thread.ownerPhone ?: info.phoneNumber
                    ),
                    conversationMessages = thread.messages
                        .sortedBy { it.messageId }
                        .map {
                        ConversationMessageUi(
                            id = it.messageId,
                            text = it.content,
                            time = formatConversationMessageTime(it.createdAt),
                            dateLabel = formatConversationDate(it.createdAt),
                            sortTimestamp = parseConversationTimestamp(it.createdAt),
                            isUser = it.isMine
                        )
                    }
                )
                refreshInbox()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoadingConversation = false,
                    conversationError = it.message ?: "Kh?ng t?i du?c tin nh?n"
                )
            }
        }
    }

    private fun clearConversationUnreadState(conversationId: Int?) {
        if (conversationId == null) return

        val clearedSections = _uiState.value.sections.map { section ->
            section.copy(
                items = section.items.map { item ->
                    if (item.category == InboxCategoryType.Message && item.conversationId == conversationId) {
                        item.copy(unread = false, badgeCount = 0)
                    } else {
                        item
                    }
                }
            )
        }

        _uiState.value = _uiState.value.copy(sections = clearedSections)
    }

    fun onDraftChanged(value: String) {
        _uiState.value = _uiState.value.copy(draftMessage = value)
    }

    fun sendMessage() {
        val conversation = _uiState.value.currentConversation ?: return
        val conversationId = conversation.conversationId ?: return
        val content = _uiState.value.draftMessage.trim()
        if (content.isEmpty()) return
        val token = token() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingMessage = true)
            runCatching { api.sendConversationMessage(token, conversationId, content) }
                .onSuccess { sent ->
                    val current = _uiState.value.conversationMessages
                    _uiState.value = _uiState.value.copy(
                        draftMessage = "",
                        isSendingMessage = false,
                        conversationMessages = (current + ConversationMessageUi(
                            id = sent.messageId,
                            text = sent.content,
                            time = formatConversationMessageTime(sent.createdAt),
                            dateLabel = formatConversationDate(sent.createdAt),
                            sortTimestamp = parseConversationTimestamp(sent.createdAt),
                            isUser = true
                        )).sortedWith(
                            compareBy<ConversationMessageUi> { it.sortTimestamp }
                                .thenBy { it.id }
                        )
                    )
                    loadConversation(conversation)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSendingMessage = false,
                        conversationError = it.message ?: "G?i tin nh?n th?t b?i"
                    )
                }
        }
    }

    fun clearConversationState() {
        _uiState.value = _uiState.value.copy(
            currentConversation = null,
            conversationMessages = emptyList(),
            conversationError = null,
            draftMessage = "",
            isLoadingConversation = false,
            isSendingMessage = false
        )
    }

    private fun buildSections(
        inboxItems: List<InboxItemDto>,
        notifications: List<NotificationDto>,
        conversations: List<ConversationListItemDto>
    ): List<NotificationSectionData> {
        val priority = mutableListOf<NotificationItem>()
        val activity = mutableListOf<NotificationItem>()
        val messages = mutableListOf<NotificationItem>()

        val notificationById = notifications.associateBy { it.id }

        inboxItems.forEach { item ->
            if (item.section.equals("messages", true)) return@forEach
            val matched = notificationById[item.id]
            val effectiveIsRead = matched?.isRead ?: item.isRead
            val mapped = mapInboxOrNotification(
                id = item.id,
                type = item.type.ifBlank { matched?.type.orEmpty() },
                section = item.section,
                title = item.title.ifBlank { matched?.title.orEmpty() },
                subtitle = item.subtitle.ifBlank { matched?.subtitle.orEmpty() },
                content = item.detail.ifBlank { matched?.content.orEmpty() },
                time = item.time,
                isRead = effectiveIsRead,
                bookingId = item.bookingId ?: matched?.bookingId,
                fieldId = item.fieldId ?: matched?.fieldId,
                conversationId = item.conversationId,
                targetType = item.targetType ?: matched?.targetType,
                targetId = item.targetId ?: matched?.targetId,
                peerUserId = matched?.peerUserId
            )
            addMappedInboxItem(
                item = mapped,
                priority = priority,
                activity = activity,
                messages = messages
            )
        }

        notifications.forEach { n ->
            val exists = priority.any { it.id == n.id } || activity.any { it.id == n.id }
            if (exists) return@forEach
            val mapped = mapInboxOrNotification(
                id = n.id,
                type = n.type,
                section = n.section,
                title = n.title,
                subtitle = n.subtitle,
                content = n.content,
                time = n.time,
                isRead = n.isRead,
                bookingId = n.bookingId,
                fieldId = n.fieldId,
                conversationId = null,
                targetType = n.targetType,
                targetId = n.targetId,
                peerUserId = n.peerUserId
            )
            addMappedInboxItem(
                item = mapped,
                priority = priority,
                activity = activity,
                messages = messages
            )
        }

        conversations.forEach { row ->
            messages.add(
                NotificationItem(
                    id = row.conversationId,
                    type = "message",
                    title = row.fieldName.ifBlank { row.ownerName ?: "Hội thoại" },
                    subtitle = row.lastMessage,
                    detail = "",
                    timeLabel = formatTime(row.lastMessageTime),
                    unread = row.unreadCount > 0,
                    badgeCount = if (row.unreadCount > 0) row.unreadCount else 0,
                    bookingId = null,
                    fieldId = row.fieldId,
                    conversationId = row.conversationId,
                    category = InboxCategoryType.Message,
                    conversationInfo = ConversationInfo(
                        fieldName = row.fieldName.ifBlank { row.ownerName ?: "Hội thoại" },
                        statusLabel = "Đang hoạt động",
                        phoneNumber = row.ownerPhone ?: "",
                        avatarRes = R.drawable.field_football,
                        conversationId = row.conversationId,
                        fieldId = row.fieldId,
                        bookingId = null
                    ),
                    icon = Icons.Outlined.ChatBubble,
                    iconBackground = Color(0x1A3F8CFF),
                    iconTint = Color(0xFF3F8CFF)
                )
            )
        }

        return listOf(
            NotificationSectionData("Ưu tiên", true, priority),
            NotificationSectionData("Tin nhắn", false, messages),
            NotificationSectionData("Hoạt động", false, activity)
        )
    }

    private fun addMappedInboxItem(
        item: NotificationItem,
        priority: MutableList<NotificationItem>,
        activity: MutableList<NotificationItem>,
        messages: MutableList<NotificationItem>
    ) {
        when (item.category) {
            InboxCategoryType.Booking -> priority.add(item)
            InboxCategoryType.Message -> messages.add(item)
            else -> activity.add(item)
        }
    }

    private suspend fun enrichBookingSections(
        token: String,
        sections: List<NotificationSectionData>
    ): List<NotificationSectionData> {
        val bookingIds = sections
            .asSequence()
            .flatMap { it.items.asSequence() }
            .filter {
                it.category == InboxCategoryType.Booking ||
                    isBookingReminderType(it.type) ||
                    isReviewReminderType(it.type)
            }
            .mapNotNull { item -> item.bookingId ?: item.bookingInfo?.bookingId }
            .distinct()
            .toList()

        if (bookingIds.isEmpty()) return sections

        val bookingDetails = mutableMapOf<Int, BookingDetailDto>()
        bookingIds.forEach { bookingId ->
            runCatching { api.getBookingDetail(token, bookingId) }
                .onSuccess { bookingDetails[bookingId] = it }
        }

        if (bookingDetails.isEmpty()) return sections

        return sections.map { section ->
            section.copy(
                items = section.items.map { item ->
                    val bookingId = item.bookingId ?: item.bookingInfo?.bookingId
                    val detail = bookingId?.let(bookingDetails::get)
                    if (
                        detail == null ||
                        (
                            item.category != InboxCategoryType.Booking &&
                                !isBookingReminderType(item.type) &&
                                !isReviewReminderType(item.type)
                        )
                    ) {
                        item
                    } else {
                        item.copy(
                            bookingInfo = detail.toBookingInfo(
                                notificationId = item.bookingInfo?.notificationId ?: item.id,
                                focusVenueInfo = item.bookingInfo?.focusVenueInfo ?:
                                    (isBookingReminderType(item.type) || isReviewReminderType(item.type)),
                                reviewFocusOnly = item.bookingInfo?.reviewFocusOnly ?: isReviewReminderType(item.type)
                            )
                        )
                    }
                }
            )
        }
    }

    private fun mapInboxOrNotification(
        id: Int,
        type: String,
        section: String,
        title: String,
        subtitle: String,
        content: String,
        time: String,
        isRead: Boolean,
        bookingId: Int?,
        fieldId: Int?,
        conversationId: Int?,
        targetType: String?,
        targetId: Int?,
        peerUserId: Int?
    ): NotificationItem {
        val normalizedType = type.lowercase()
        val normalizedSection = section.lowercase()
        val isMatchRequestNotification =
            normalizedType == "match_request_received" ||
                normalizedType == "match_request_accepted" ||
                normalizedType == "match_request_rejected"
        val isReviewReminderNotification = isReviewReminderType(normalizedType)
        val effectiveBookingId =
            bookingId ?: if (targetType.equals("booking", ignoreCase = true)) targetId else null

        val category = when {
            normalizedSection == "messages" || normalizedType == "message" -> InboxCategoryType.Message
            isMatchRequestNotification -> InboxCategoryType.Activity
            isBookingReminderType(normalizedType) -> InboxCategoryType.Activity
            isReviewReminderNotification -> InboxCategoryType.Activity
            isBookingLifecycleType(normalizedType) ||
                targetType.equals("booking", ignoreCase = true) -> InboxCategoryType.Booking
            else -> InboxCategoryType.Activity
        }

        val conversationInfo = if (category == InboxCategoryType.Message) {
            ConversationInfo(
                fieldName = title.ifBlank { "Hội thoại" },
                statusLabel = "Đang hoạt động",
                phoneNumber = "",
                avatarRes = R.drawable.field_football,
                conversationId = conversationId ?: targetId,
                fieldId = fieldId,
                bookingId = effectiveBookingId,
                peerUserId = peerUserId
            )
        } else null

        val bookingInfo = if (
            category == InboxCategoryType.Booking ||
            isBookingReminderType(normalizedType) ||
            isReviewReminderNotification
        ) {
            BookingInfo(
                fieldName = if (isReviewReminderNotification) subtitle.ifBlank { title } else title,
                timeRange = "",
                dateLabel = time,
                bookingCode = effectiveBookingId?.let { "#B$it" } ?: "",
                statusLabel = if (isReviewReminderNotification) "Cần đánh giá" else "confirmed",
                address = "",
                paymentMethod = "",
                totalAmount = "",
                customerName = "",
                customerPhone = "",
                ownerPhone = "",
                ownerNote = if (isReviewReminderNotification) content.ifBlank { subtitle } else subtitle,
                fieldId = fieldId,
                bookingId = effectiveBookingId,
                notificationId = id,
                focusVenueInfo = isBookingReminderType(normalizedType) || isReviewReminderNotification,
                reviewFocusOnly = isReviewReminderNotification
            )
        } else null

        val detailInfo = when (normalizedType) {
            "upcoming_match" -> NotificationDetailInfo.UpcomingMatch(
                title = title,
                subtitle = subtitle,
                notificationId = id,
                fieldId = fieldId,
                bookingId = effectiveBookingId,
                fieldName = title,
                address = "",
                timeRange = "",
                dateLabel = "",
                bookingCode = effectiveBookingId?.let { "#B$it" } ?: "",
                statusLabel = "confirmed",
                paymentMethod = "",
                totalAmount = "",
                reminderText = content.ifBlank { subtitle },
                phoneNumber = "",
                avatarRes = R.drawable.field_football
            )
            "match_request_received" -> NotificationDetailInfo.MatchRequestNotice(
                title = displayInboxTitle(normalizedType, title),
                subtitle = matchNotificationSummary(normalizedType, title, subtitle),
                notificationId = id,
                bookingId = effectiveBookingId,
                fieldId = fieldId,
                matchRequestId = if (targetType.equals("match_request", ignoreCase = true)) targetId else null,
                peerUserId = peerUserId,
                fieldName = "",
                address = "",
                timeRange = "",
                dateLabel = "",
                bookingCode = effectiveBookingId?.let { "#B$it" } ?: "",
                hostTeamName = subtitle,
                requesterTeamName = title.substringBefore(" muốn").ifBlank { title },
                requesterAccountName = "",
                requesterPlayerCount = 0,
                requesterLevelLabel = "",
                requesterMessage = content.ifBlank { subtitle },
                requestStatus = "PENDING",
                canRespond = true,
                timeText = formatTime(time)
            )
            "match_request_accepted" -> NotificationDetailInfo.MatchRequestNotice(
                title = displayInboxTitle(normalizedType, title),
                subtitle = matchNotificationSummary(normalizedType, title, subtitle),
                notificationId = id,
                bookingId = effectiveBookingId,
                fieldId = fieldId,
                matchRequestId = if (targetType.equals("match_request", ignoreCase = true)) targetId else null,
                peerUserId = peerUserId,
                fieldName = "",
                address = "",
                timeRange = "",
                dateLabel = "",
                bookingCode = effectiveBookingId?.let { "#B$it" } ?: "",
                hostTeamName = subtitle,
                requesterTeamName = subtitle,
                requesterAccountName = "",
                requesterPlayerCount = 0,
                requesterLevelLabel = "",
                requesterMessage = content.ifBlank { subtitle },
                requestStatus = "ACCEPTED",
                canRespond = false,
                timeText = formatTime(time)
            )
            "promotion" -> NotificationDetailInfo.Promotion(
                title = title,
                subtitle = subtitle,
                notificationId = id,
                promoTitle = title,
                promoSubtitle = subtitle,
                contentText = content.ifBlank { subtitle },
                periodText = "",
                conditions = emptyList()
            )
            "system", "system_notice", "match_request_rejected" -> NotificationDetailInfo.SystemNotice(
                title = displayInboxTitle(normalizedType, title),
                subtitle = matchNotificationSummary(normalizedType, title, subtitle),
                notificationId = id,
                contentText = content.ifBlank { subtitle },
                features = emptyList(),
                timeText = formatTime(time)
            )
            else -> null
        }

        val icon = when (normalizedType) {
            "match_request_received" -> Icons.Outlined.Person
            "match_request_accepted" -> Icons.Outlined.CheckCircle
            "match_request_rejected" -> Icons.Outlined.Close
            "booking_reminder" -> Icons.Outlined.Schedule
            "booking_reminder_urgent" -> Icons.Outlined.EventAvailable
            else -> when (category) {
                InboxCategoryType.Booking -> Icons.Outlined.EventAvailable
                InboxCategoryType.Message -> Icons.Outlined.ChatBubble
                else -> Icons.Outlined.Notifications
            }
        }

        val iconBackground = when (normalizedType) {
            "match_request_received" -> Color(0x1AF59E0B)
            "match_request_accepted" -> Color(0x1A16A34A)
            "match_request_rejected" -> Color(0x1ADC2626)
            "booking_reminder" -> Color(0x1A2563EB)
            "booking_reminder_urgent" -> Color(0x1AF97316)
            else -> Color(0x1A3F8CFF)
        }

        val iconTint = when (normalizedType) {
            "match_request_received" -> Color(0xFFD97706)
            "match_request_accepted" -> Color(0xFF16A34A)
            "match_request_rejected" -> Color(0xFFDC2626)
            "booking_reminder" -> Color(0xFF2563EB)
            "booking_reminder_urgent" -> Color(0xFFF97316)
            else -> Color(0xFF3F8CFF)
        }

        return NotificationItem(
            id = id,
            type = type,
            title = displayInboxTitle(normalizedType, title),
            subtitle = if (isMatchRequestNotification) {
                matchNotificationSummary(normalizedType, title, subtitle)
            } else {
                subtitle
            },
            detail = if (category == InboxCategoryType.Activity && !isMatchRequestNotification) content else "",
            timeLabel = formatTime(time),
            unread = !isRead,
            badgeCount = if (!isRead) 1 else 0,
            bookingId = effectiveBookingId,
            fieldId = fieldId,
            conversationId = conversationId ?: targetId,
            category = category,
            bookingInfo = bookingInfo,
            conversationInfo = conversationInfo,
            detailInfo = detailInfo,
            icon = icon,
            iconBackground = iconBackground,
            iconTint = iconTint
        )
    }

    private fun token(): String? = prefs.getString("auth_token", null)?.takeIf { it.isNotBlank() }

    private fun formatTime(raw: String): String {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val timePattern = DateTimeFormatter.ofPattern("HH:mm")

        return runCatching {
            val odt = OffsetDateTime.parse(raw)
            odt.atZoneSameInstant(zone).format(timePattern)
        }.recoverCatching {
            val ldt = LocalDateTime.parse(raw)
            ldt.atZone(zone).format(timePattern)
        }.recoverCatching {
            val ldt = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).format(timePattern)
        }.getOrDefault(raw)
    }

    private fun formatConversationMessageTime(raw: String): String {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val timePattern = DateTimeFormatter.ofPattern("HH:mm")

        return runCatching {
            val odt = OffsetDateTime.parse(raw)
            odt.atZoneSameInstant(zone).format(timePattern)
        }.recoverCatching {
            val ldt = LocalDateTime.parse(raw)
            ldt.atZone(zone).format(timePattern)
        }.recoverCatching {
            val ldt = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).format(timePattern)
        }.getOrDefault(raw)
    }

    private fun formatConversationDate(raw: String): String {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")
        val datePattern = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return runCatching {
            val odt = OffsetDateTime.parse(raw)
            odt.atZoneSameInstant(zone).format(datePattern)
        }.recoverCatching {
            val ldt = LocalDateTime.parse(raw)
            ldt.atZone(zone).format(datePattern)
        }.recoverCatching {
            val ldt = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ldt.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).format(datePattern)
        }.getOrDefault(raw)
    }

    private fun parseConversationTimestamp(raw: String): Long {
        val zone = ZoneId.of("Asia/Ho_Chi_Minh")

        return runCatching {
            OffsetDateTime.parse(raw).toInstant().toEpochMilli()
        }.recoverCatching {
            LocalDateTime.parse(raw).atZone(zone).toInstant().toEpochMilli()
        }.recoverCatching {
            LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .atZone(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrDefault(Long.MAX_VALUE)
    }

    private fun BookingDetailDto.toBookingInfo(
        notificationId: Int?,
        focusVenueInfo: Boolean = false,
        reviewFocusOnly: Boolean = false
    ): BookingInfo {
        val resolvedTimeRange = timeRange.ifBlank {
            listOf(startTime, endTime)
                .filter { it.isNotBlank() }
                .joinToString(" - ")
        }

        return BookingInfo(
            fieldName = fieldName,
            timeRange = resolvedTimeRange,
            dateLabel = formatBookingDate(date),
            bookingCode = bookingCode,
            statusLabel = status,
            statusCode = statusCode,
            address = fieldAddress,
            paymentMethod = paymentMethod,
            totalAmount = totalPrice,
            transactionId = transactionId,
            orderId = orderId,
            checkInCode = checkInCode,
            shareUrl = shareUrl,
            customerName = userName,
            customerPhone = userPhone,
            ownerPhone = ownerPhone,
            ownerNote = ownerNote,
            fieldId = fieldId,
            bookingId = bookingId,
            notificationId = notificationId,
            canReview = canReview,
            reviewSubmitted = reviewSubmitted,
            reviewId = reviewId,
            reviewRating = reviewRating,
            reviewComment = reviewComment,
            matchPost = matchPost?.let {
                BookingMatchPostInfo(
                    matchPostId = it.matchPostId,
                    ownerUserId = it.ownerUserId,
                    ownerUsername = it.ownerUsername,
                    teamName = it.teamName,
                    playerCount = it.playerCount,
                    level = it.level,
                    levelLabel = it.levelLabel,
                    description = it.description,
                    status = it.status
                )
            },
            matchRequests = matchRequests.map {
                BookingMatchRequestInfo(
                    matchRequestId = it.matchRequestId,
                    requesterUserId = it.requesterUserId,
                    requesterUsername = it.requesterUsername,
                    teamName = it.teamName,
                    playerCount = it.playerCount,
                    level = it.level,
                    levelLabel = it.levelLabel,
                    message = it.message,
                    status = it.status,
                    createdAt = it.createdAt
                )
            },
            focusVenueInfo = focusVenueInfo,
            reviewFocusOnly = reviewFocusOnly
        )
    }

    private fun formatBookingDate(raw: String): String {
        val output = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return runCatching {
            LocalDate.parse(raw).format(output)
        }.recoverCatching {
            LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd")).format(output)
        }.recoverCatching {
            LocalDateTime.parse(raw).toLocalDate().format(output)
        }.recoverCatching {
            OffsetDateTime.parse(raw).toLocalDate().format(output)
        }.recoverCatching {
            LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate().format(output)
        }.getOrDefault(raw)
    }

    private fun buildMatchRequestDetail(
        detail: NotificationDto,
        booking: BookingDetailDto?
    ): NotificationDetailInfo {
        val resolvedMatchRequestId = if (detail.targetType.equals("match_request", ignoreCase = true)) {
            detail.targetId
        } else {
            null
        }

        val request = booking?.matchRequests?.firstOrNull { request ->
            resolvedMatchRequestId != null && request.matchRequestId == resolvedMatchRequestId
        } ?: booking?.matchRequests?.firstOrNull { request ->
            request.status.equals("PENDING", ignoreCase = true)
        } ?: booking?.matchRequests?.firstOrNull()

        val requestStatus = if (detail.type.equals("match_request_accepted", ignoreCase = true)) {
            request?.status ?: "ACCEPTED"
        } else {
            request?.status ?: "PENDING"
        }
        val resolvedCounterpartTeamName = when {
            detail.type.equals("match_request_received", ignoreCase = true) ->
                request?.teamName ?: detail.title.substringBefore(" muốn").ifBlank { detail.title }
            detail.type.equals("match_request_accepted", ignoreCase = true) ->
                booking?.matchPost?.teamName ?: detail.subtitle
            else -> request?.teamName ?: detail.subtitle
        }
        val resolvedPeerUserId = when {
            detail.type.equals("match_request_received", ignoreCase = true) ->
                request?.requesterUserId ?: detail.peerUserId
            detail.type.equals("match_request_accepted", ignoreCase = true) ->
                booking?.matchPost?.ownerUserId ?: detail.peerUserId
            else -> detail.peerUserId ?: request?.requesterUserId
        }

        val bookingCode = booking?.bookingCode ?: (detail.bookingId?.let { "#B$it" } ?: "")
        val fieldId = detail.fieldId ?: booking?.fieldId
        val timeRange = booking?.timeRange?.ifBlank {
            listOf(booking.startTime, booking.endTime)
                .filter { it.isNotBlank() }
                .joinToString(" - ")
        } ?: ""
        val dateLabel = booking?.date?.let(::formatBookingDate) ?: ""
        val hostTeamName = booking?.matchPost?.teamName ?: detail.subtitle
        val requesterTeamName = resolvedCounterpartTeamName
        val ownerUsername = booking?.matchPost?.ownerUsername ?: ""
        val requesterAccountName = request?.requesterUsername ?: ""
        val requesterPlayerCount = request?.playerCount ?: 0
        val requesterLevelLabel = request?.levelLabel ?: ""
        val requesterMessage = request?.message?.ifBlank { detail.content.ifBlank { detail.subtitle } }
            ?: detail.content.ifBlank { detail.subtitle }
        val timeText = formatTime(detail.time)

        return when {
            detail.type.equals("match_request_accepted", ignoreCase = true) -> {
                NotificationDetailInfo.MatchSuccessNotice(
                    title = displayInboxTitle(detail.type.lowercase(), detail.title),
                    subtitle = matchNotificationSummary(detail.type, detail.title, detail.subtitle),
                    notificationId = detail.id,
                    bookingId = detail.bookingId,
                    fieldId = fieldId,
                    requesterUserId = resolvedPeerUserId,
                    fieldName = booking?.fieldName ?: "",
                    address = booking?.fieldAddress ?: "",
                    timeRange = timeRange,
                    dateLabel = dateLabel,
                    bookingCode = bookingCode,
                    checkInCode = booking?.checkInCode ?: "",
                    shareUrl = booking?.shareUrl ?: "",
                    hostTeamName = hostTeamName,
                    requesterTeamName = requesterTeamName,
                    ownerUsername = ownerUsername,
                    requesterAccountName = ownerUsername,
                    requesterPlayerCount = requesterPlayerCount,
                    requesterLevelLabel = requesterLevelLabel,
                    requesterMessage = requesterMessage,
                    requestStatus = requestStatus,
                    timeText = timeText
                )
            }
            else -> {
                NotificationDetailInfo.MatchRequestNotice(
                    title = displayInboxTitle(detail.type.lowercase(), detail.title),
                    subtitle = matchNotificationSummary(detail.type, detail.title, detail.subtitle),
                    notificationId = detail.id,
                    bookingId = detail.bookingId,
                    fieldId = fieldId,
                    matchRequestId = request?.matchRequestId ?: resolvedMatchRequestId,
                    peerUserId = resolvedPeerUserId,
                    fieldName = booking?.fieldName ?: "",
                    address = booking?.fieldAddress ?: "",
                    timeRange = timeRange,
                    dateLabel = dateLabel,
                    bookingCode = bookingCode,
                    hostTeamName = hostTeamName,
                    requesterTeamName = requesterTeamName,
                    requesterAccountName = requesterAccountName,
                    requesterPlayerCount = requesterPlayerCount,
                    requesterLevelLabel = requesterLevelLabel,
                    requesterMessage = requesterMessage,
                    requestStatus = requestStatus,
                    canRespond = detail.type.equals("match_request_received", ignoreCase = true) &&
                        requestStatus.equals("PENDING", ignoreCase = true),
                    timeText = timeText
                )
            }
        }
    }

    private fun BookingInfo.applyMatchRequestUpdate(
        matchRequestId: Int,
        accept: Boolean
    ): BookingInfo {
        val updatedRequests = matchRequests.map { request ->
            when {
                request.matchRequestId == matchRequestId -> request.copy(
                    status = if (accept) "ACCEPTED" else "REJECTED"
                )
                accept && request.status.equals("PENDING", ignoreCase = true) -> request.copy(
                    status = "REJECTED"
                )
                else -> request
            }
        }

        return copy(
            matchPost = matchPost?.copy(
                status = if (accept) "MATCHED" else matchPost.status
            ),
            matchRequests = updatedRequests
        )
    }

    private fun NotificationDetailInfo.MatchRequestNotice.applyMatchRequestUpdate(
        accept: Boolean
    ): NotificationDetailInfo.MatchRequestNotice {
        return copy(
            requestStatus = if (accept) "ACCEPTED" else "REJECTED",
            canRespond = false
        )
    }
}

    private fun isBookingReminderType(type: String?): Boolean {
        val normalized = type?.lowercase().orEmpty()
        return normalized == "booking_reminder" ||
            normalized == "booking_reminder_urgent" ||
            normalized == "upcoming_match"
    }

    private fun displayInboxTitle(type: String, title: String): String {
        return when (type.lowercase()) {
            "match_request_received" -> "Yêu cầu ghép trận"
            "match_request_accepted" -> "Ghép trận thành công"
            else -> title
        }
    }

    private fun matchNotificationSummary(type: String, title: String, subtitle: String): String {
        return when (type.lowercase()) {
            "match_request_received" -> {
                val requesterTeam = normalizeTeamLabel(
                    title.substringBefore(" muốn ghép trận với bạn")
                        .substringBefore(" muốn giao lưu với bạn")
                        .ifBlank { title }
                )
                "$requesterTeam muốn giao lưu với bạn"
            }
            "match_request_accepted" -> {
                val ownerTeam = normalizeTeamLabel(subtitle.ifBlank { "Đội chủ sân" })
                "$ownerTeam đã đồng ý ghép trận với bạn"
            }
            "match_request_rejected" -> {
                val ownerTeam = normalizeTeamLabel(subtitle.ifBlank { "Đội chủ sân" })
                "$ownerTeam đã từ chối ghép trận với bạn"
            }
            else -> subtitle
        }
    }

    private fun normalizeTeamLabel(raw: String): String {
        val trimmed = raw.trim().trimEnd('.')
        return if (trimmed.startsWith("Đội ", ignoreCase = true)) trimmed else "Đội $trimmed"
    }

    private fun isReviewReminderType(type: String?): Boolean {
        val normalized = type?.lowercase().orEmpty()
        return normalized == "review_reminder" ||
            normalized == "review_request" ||
            normalized == "booking_completed" ||
        normalized == "field_review_request"
}

private fun isBookingLifecycleType(type: String?): Boolean {
    val normalized = type?.lowercase().orEmpty()
    return normalized == "booking_success" ||
        normalized == "booking_confirmed" ||
        normalized == "booking_cancelled" ||
        normalized == "booking"
}

private fun NotificationItem.matchesNotificationId(notificationId: Int): Boolean {
    val detailNotificationId = when (val detail = detailInfo) {
        is NotificationDetailInfo.UpcomingMatch -> detail.notificationId
        is NotificationDetailInfo.Promotion -> detail.notificationId
        is NotificationDetailInfo.SystemNotice -> detail.notificationId
        is NotificationDetailInfo.MatchRequestNotice -> detail.notificationId
        is NotificationDetailInfo.MatchSuccessNotice -> detail.notificationId
        null -> null
    }

    return id == notificationId ||
        bookingInfo?.notificationId == notificationId ||
        detailNotificationId == notificationId
}

data class InboxUiState(
    val isLoadingInbox: Boolean = false,
    val inboxError: String? = null,
    val sections: List<NotificationSectionData> = emptyList(),
    val isLoadingBookingDetail: Boolean = false,
    val bookingDetailError: String? = null,
    val activeBookingDetail: BookingInfo? = null,
    val isLoadingNotificationDetail: Boolean = false,
    val activeNotificationDetail: NotificationDetailInfo? = null,
    val currentConversation: ConversationInfo? = null,
    val isLoadingConversation: Boolean = false,
    val conversationError: String? = null,
    val conversationMessages: List<ConversationMessageUi> = emptyList(),
    val draftMessage: String = "",
    val isSendingMessage: Boolean = false,
    val processingMatchRequestId: Int? = null,
    val matchRequestActionError: String? = null,
    val matchRequestActionSuccessMessage: String? = null,
    val isSubmittingReview: Boolean = false,
    val reviewSubmissionError: String? = null,
    val reviewSubmissionSuccessMessage: String? = null,
)

class InboxViewModelFactory(
    private val appContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InboxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InboxViewModel(appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
