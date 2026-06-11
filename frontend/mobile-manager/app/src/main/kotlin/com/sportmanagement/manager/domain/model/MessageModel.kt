package com.sportmanagement.manager.domain.model

enum class MessageStatus { SENDING, SENT, FAILED }

data class ConversationItem(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val customerAvatarUrl: String?,
    val isOnline: Boolean,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val totalBookings: Int = 0
)

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromManager: Boolean,
    val timestamp: String,
    val rawTimestamp: String? = null,
    val isRead: Boolean = true,
    val status: MessageStatus = MessageStatus.SENT
)

data class ReviewItem(
    val id: String,
    val customerName: String,
    val customerAvatarUrl: String?,
    val rating: Int,
    val content: String,
    val timestamp: String,
    val pitchName: String,
    val courtName: String = "",
    val managerReply: String? = null,
    val replyTimestamp: String? = null
)
