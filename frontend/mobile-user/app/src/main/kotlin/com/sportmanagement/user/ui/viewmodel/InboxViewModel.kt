package com.sportmanagement.user.ui.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sportmanagement.user.R
import com.sportmanagement.user.data.remote.api.ConversationListItemDto
import com.sportmanagement.user.data.remote.api.InboxApi
import com.sportmanagement.user.data.remote.api.InboxItemDto
import com.sportmanagement.user.data.remote.api.NotificationDto
import com.sportmanagement.user.ui.screens.BookingInfo
import com.sportmanagement.user.ui.screens.ConversationInfo
import com.sportmanagement.user.ui.screens.ConversationMessageUi
import com.sportmanagement.user.ui.screens.InboxCategoryType
import com.sportmanagement.user.ui.screens.NotificationDetailInfo
import com.sportmanagement.user.ui.screens.NotificationItem
import com.sportmanagement.user.ui.screens.NotificationSectionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
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

    init {
        refreshInbox()
    }

    fun refreshInbox() {
        val token = token()
        if (token == null) {
            _uiState.value = _uiState.value.copy(
                isLoadingInbox = false,
                sections = emptyList(),
                inboxError = "Ban chua dang nhap. Vui long dang nhap de xem hop thu."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingInbox = true, inboxError = null)

            val inboxItems = runCatching { api.getInbox(token) }.getOrDefault(emptyList())
            val notifications = runCatching { api.getNotifications(token) }.getOrDefault(emptyList())
            val conversations = runCatching { api.getConversations(token) }.getOrDefault(emptyList())

            val sections = buildSections(inboxItems, notifications, conversations)
            _uiState.value = _uiState.value.copy(
                isLoadingInbox = false,
                sections = sections,
                inboxError = if (sections.all { it.items.isEmpty() }) "Hop thu chua co du lieu." else null
            )
        }
    }

    fun markAllRead() {
        val token = token() ?: return

        val unreadConversationIds = _uiState.value.sections
            .flatMap { it.items }
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
            runCatching { api.markAllNotificationsRead(token) }
            unreadConversationIds.forEach { conversationId ->
                runCatching { api.markConversationRead(token, conversationId) }
                    .recoverCatching {
                        // Fallback: opening thread API also marks incoming messages as read on backend.
                        api.getConversationMessages(token, conversationId)
                    }
            }
            refreshInbox()
        }
    }

    fun markNotificationRead(notificationId: Int?) {
        val token = token() ?: return
        val id = notificationId ?: return
        viewModelScope.launch {
            runCatching { api.markNotificationRead(token, id) }
            refreshInbox()
        }
    }

    fun loadBookingDetail(bookingId: Int?, notificationId: Int? = null) {
        val token = token() ?: return
        viewModelScope.launch {
            runCatching {
                val resolvedBookingId = bookingId ?: notificationId?.let { nid ->
                    val detail = api.getNotificationDetail(token, nid)
                    detail.bookingId ?: detail.targetId
                }
                resolvedBookingId?.let { api.getBookingDetail(token, it) }
            }
                .onSuccess { booking ->
                    if (booking == null) return@onSuccess
                    val dateLabel = booking.date.ifBlank { "" }
                    val timeRange = booking.timeRange.ifBlank {
                        listOf(booking.startTime, booking.endTime)
                            .filter { it.isNotBlank() }
                            .joinToString(" - ")
                    }

                    _uiState.value = _uiState.value.copy(
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
                            notificationId = null
                        )
                    )
                }
        }
    }

    fun clearActiveBookingDetail() {
        _uiState.value = _uiState.value.copy(activeBookingDetail = null)
    }

    fun loadNotificationDetail(notificationId: Int?, fallback: NotificationDetailInfo?) {
        val token = token() ?: return
        val id = notificationId ?: run {
            _uiState.value = _uiState.value.copy(activeNotificationDetail = fallback)
            return
        }

        viewModelScope.launch {
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
                    "system", "system_notice" -> NotificationDetailInfo.SystemNotice(
                        title = detail.title,
                        subtitle = detail.subtitle,
                        notificationId = detail.id,
                        contentText = detail.content.ifBlank { detail.subtitle },
                        features = emptyList(),
                        timeText = formatTime(detail.time)
                    )
                    else -> fallback
                }
            }.onSuccess { resolved ->
                _uiState.value = _uiState.value.copy(activeNotificationDetail = resolved)
            }.onFailure {
                _uiState.value = _uiState.value.copy(activeNotificationDetail = fallback)
            }
        }
    }

    fun clearActiveNotificationDetail() {
        _uiState.value = _uiState.value.copy(activeNotificationDetail = null)
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
                    bookingId = info.bookingId
                ).conversationId

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
                            time = formatTime(it.createdAt),
                            isUser = it.isMine
                        )
                    }
                )
                refreshInbox()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoadingConversation = false,
                    conversationError = it.message ?: "Khong tai duoc tin nhan"
                )
            }
        }
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
                            time = formatTime(sent.createdAt),
                            isUser = true
                        )).sortedBy { it.id }
                    )
                    loadConversation(conversation)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSendingMessage = false,
                        conversationError = it.message ?: "Gui tin nhan that bai"
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
            val mapped = mapInboxOrNotification(
                id = item.id,
                type = item.type.ifBlank { matched?.type.orEmpty() },
                section = item.section,
                title = item.title.ifBlank { matched?.title.orEmpty() },
                subtitle = item.subtitle.ifBlank { matched?.subtitle.orEmpty() },
                content = item.detail.ifBlank { matched?.content.orEmpty() },
                time = item.time,
                isRead = item.isRead,
                bookingId = item.bookingId ?: matched?.bookingId,
                fieldId = item.fieldId ?: matched?.fieldId,
                conversationId = item.conversationId,
                targetType = item.targetType ?: matched?.targetType,
                targetId = item.targetId ?: matched?.targetId
            )
            if (item.section.equals("priority", true)) priority.add(mapped) else activity.add(mapped)
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
                targetId = n.targetId
            )
            if (n.section.equals("priority", true)) priority.add(mapped) else activity.add(mapped)
        }

        conversations.forEach { row ->
            messages.add(
                NotificationItem(
                    id = row.conversationId,
                    type = "message",
                    title = row.fieldName.ifBlank { row.ownerName ?: "Hoi thoai" },
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
                        fieldName = row.fieldName.ifBlank { row.ownerName ?: "Hoi thoai" },
                        statusLabel = "Dang hoat dong",
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
        targetId: Int?
    ): NotificationItem {
        val normalizedType = type.lowercase()
        val normalizedSection = section.lowercase()
        val effectiveBookingId =
            bookingId ?: if (targetType.equals("booking", ignoreCase = true)) targetId else null

        val category = when {
            normalizedSection == "messages" || normalizedType == "message" -> InboxCategoryType.Message
            normalizedType == "booking_success" ||
                normalizedType == "booking" ||
                targetType.equals("booking", ignoreCase = true) -> InboxCategoryType.Booking
            else -> InboxCategoryType.Activity
        }

        val conversationInfo = if (category == InboxCategoryType.Message) {
            ConversationInfo(
                fieldName = title.ifBlank { "Hoi thoai" },
                statusLabel = "Dang hoat dong",
                phoneNumber = "",
                avatarRes = R.drawable.field_football,
                conversationId = conversationId ?: targetId,
                fieldId = fieldId,
                bookingId = effectiveBookingId
            )
        } else null

        val bookingInfo = if (category == InboxCategoryType.Booking) {
            BookingInfo(
                fieldName = title,
                timeRange = "",
                dateLabel = time,
                bookingCode = effectiveBookingId?.let { "#B$it" } ?: "",
                statusLabel = "confirmed",
                address = "",
                paymentMethod = "",
                totalAmount = "",
                customerName = "",
                customerPhone = "",
                ownerPhone = "",
                ownerNote = subtitle,
                fieldId = fieldId,
                bookingId = effectiveBookingId,
                notificationId = id
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
            "system", "system_notice" -> NotificationDetailInfo.SystemNotice(
                title = title,
                subtitle = subtitle,
                notificationId = id,
                contentText = content.ifBlank { subtitle },
                features = emptyList(),
                timeText = formatTime(time)
            )
            else -> null
        }

        val icon = when (category) {
            InboxCategoryType.Booking -> Icons.Outlined.EventAvailable
            InboxCategoryType.Message -> Icons.Outlined.ChatBubble
            else -> Icons.Outlined.Notifications
        }

        return NotificationItem(
            id = id,
            type = type,
            title = title,
            subtitle = subtitle,
            detail = if (category == InboxCategoryType.Activity) content else "",
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
            iconBackground = Color(0x1A3F8CFF),
            iconTint = Color(0xFF3F8CFF)
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
}

data class InboxUiState(
    val isLoadingInbox: Boolean = false,
    val inboxError: String? = null,
    val sections: List<NotificationSectionData> = emptyList(),
    val activeBookingDetail: BookingInfo? = null,
    val activeNotificationDetail: NotificationDetailInfo? = null,
    val currentConversation: ConversationInfo? = null,
    val isLoadingConversation: Boolean = false,
    val conversationError: String? = null,
    val conversationMessages: List<ConversationMessageUi> = emptyList(),
    val draftMessage: String = "",
    val isSendingMessage: Boolean = false
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
