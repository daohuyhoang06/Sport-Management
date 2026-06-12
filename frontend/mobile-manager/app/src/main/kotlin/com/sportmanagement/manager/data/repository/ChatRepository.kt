package com.sportmanagement.manager.data.repository

import com.sportmanagement.manager.data.local.db.ChatDatabase
import com.sportmanagement.manager.data.local.db.entity.ConversationEntity
import com.sportmanagement.manager.data.local.db.entity.MessageEntity
import com.sportmanagement.manager.data.remote.api.ChatApiService
import com.sportmanagement.manager.data.remote.dto.ChatListDto
import com.sportmanagement.manager.data.remote.dto.ChatMessageDto
import com.sportmanagement.manager.data.remote.dto.SendMessageRequest
import com.sportmanagement.manager.data.remote.dto.StartChatRequest

class ChatRepository(
    private val api: ChatApiService,
    private val db: ChatDatabase
) {

    // ── Cache reads ───────────────────────────────────────────────────────────

    suspend fun getCachedConversations(): List<ChatListDto> {
        return try {
            db.conversationDao().getAllConversations().map { it.toDto() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCachedMessages(chatId: Int): List<ChatMessageDto> {
        return try {
            db.messageDao().getMessages(chatId).map { it.toDto() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markConversationAsRead(chatId: Int) {
        try { db.conversationDao().markAsRead(chatId) } catch (e: Exception) {}
    }

    // ── Network + cache write ─────────────────────────────────────────────────

    suspend fun getChatList(): Result<List<ChatListDto>> {
        return try {
            val response = api.getChatList()
            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                cacheConversations(data)
                Result.success(data)
            } else {
                Result.failure(Exception("Lỗi tải danh sách chat (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }

    suspend fun getMessages(chatId: Int): Result<List<ChatMessageDto>> {
        return try {
            val response = api.getMessages(chatId)
            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                cacheMessages(data)
                Result.success(data)
            } else {
                Result.failure(Exception("Lỗi tải tin nhắn (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }

    suspend fun getOrCreateChatWith(userId: Int): Result<ChatListDto> {
        return try {
            val response = api.startManagerChat(StartChatRequest(userId))
            if (response.isSuccessful) {
                val data = response.body()?.data
                    ?: return Result.failure(Exception("Không thể khởi tạo chat"))
                Result.success(ChatListDto(
                    chatId = data.chatId,
                    customerId = userId,
                    managerId = data.managerId,
                    customerName = null,
                    customerPhone = null,
                    customerAvatar = null,
                    lastMessage = null,
                    lastMessageTime = null,
                    unreadCount = 0
                ))
            } else {
                Result.failure(Exception("Lỗi khởi tạo chat (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }

    suspend fun sendMessage(chatId: Int, content: String): Result<Unit> {
        return try {
            val response = api.sendMessage(chatId, SendMessageRequest(content))
            if (response.isSuccessful) {
                // API succeeded: try to save to cache but don't fail if Room errors
                try {
                    response.body()?.data?.let { dto -> cacheMessage(dto) }
                } catch (e: Exception) {}
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gửi tin nhắn thất bại (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }

    // ── Cache helpers (silent failures) ──────────────────────────────────────

    private suspend fun cacheConversations(data: List<ChatListDto>) {
        try {
            val now = System.currentTimeMillis()
            db.conversationDao().insertConversations(data.map { it.toEntity(now) })
        } catch (e: Exception) {}
    }

    private suspend fun cacheMessages(data: List<ChatMessageDto>) {
        try {
            db.messageDao().insertMessages(data.map { it.toEntity() })
        } catch (e: Exception) {}
    }

    private suspend fun cacheMessage(dto: ChatMessageDto) {
        db.messageDao().insertMessage(dto.toEntity())
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private fun ChatListDto.toEntity(savedAt: Long): ConversationEntity =
        ConversationEntity(
            chatId = chatId,
            customerId = customerId ?: 0,
            managerId = managerId ?: 0,
            customerName = customerName ?: "",
            customerPhone = customerPhone,
            customerAvatar = customerAvatar,
            lastMessage = lastMessage,
            lastMessageTime = lastMessageTime,
            unreadCount = unreadCount ?: 0,
            savedAt = savedAt
        )

    private fun ConversationEntity.toDto(): ChatListDto =
        ChatListDto(
            chatId = chatId,
            customerId = customerId,
            managerId = managerId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAvatar = customerAvatar,
            lastMessage = lastMessage,
            lastMessageTime = lastMessageTime,
            unreadCount = unreadCount
        )

    private fun ChatMessageDto.toEntity(): MessageEntity =
        MessageEntity(
            messageId = messageId,
            chatId = chatId,
            senderId = senderId,
            content = content ?: "",
            sentAt = sentAt ?: "",
            isRead = isRead ?: false
        )

    private fun MessageEntity.toDto(): ChatMessageDto =
        ChatMessageDto(
            messageId = messageId,
            chatId = chatId,
            senderId = senderId,
            content = content,
            sentAt = sentAt,
            isRead = isRead
        )
}
