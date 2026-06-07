package com.sportmanagement.manager.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatListDto(
    @SerializedName("chat_id") val chatId: Int,
    @SerializedName("customer_id") val customerId: Int?,
    @SerializedName("manager_id") val managerId: Int?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("customer_phone") val customerPhone: String?,
    @SerializedName("customer_avatar") val customerAvatar: String?,
    @SerializedName("last_message") val lastMessage: String?,
    @SerializedName("last_message_time") val lastMessageTime: String?,
    @SerializedName("unread_count") val unreadCount: Int?
)

data class ChatMessageDto(
    @SerializedName("message_id") val messageId: Int,
    @SerializedName("chat_id") val chatId: Int,
    @SerializedName("sender_id") val senderId: Int,
    val content: String?,
    @SerializedName("sent_at") val sentAt: String?,
    @SerializedName("is_read") val isRead: Boolean?
)

data class SendMessageRequest(val content: String)

data class SendMessageResponse(
    val success: Boolean?,
    val message: String?,
    val data: ChatMessageDto?
)

data class ChatListResponse(
    val success: Boolean?,
    val data: List<ChatListDto>?
)

data class ChatMessagesResponse(
    val success: Boolean?,
    val data: List<ChatMessageDto>?
)
