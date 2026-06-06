package com.sportmanagement.manager.data.repository

import com.sportmanagement.manager.data.remote.api.ChatApiService
import com.sportmanagement.manager.data.remote.dto.ChatListDto
import com.sportmanagement.manager.data.remote.dto.ChatMessageDto
import com.sportmanagement.manager.data.remote.dto.SendMessageRequest

class ChatRepository(private val api: ChatApiService) {

    suspend fun getChatList(): Result<List<ChatListDto>> = safeCall {
        val response = api.getChatList()
        if (response.isSuccessful) {
            // Hỗ trợ cả response wrapped {success, data:[]} và raw array
            val body = response.body()
            Result.success(body?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải danh sách chat (${response.code()})"))
        }
    }

    suspend fun getMessages(chatId: Int): Result<List<ChatMessageDto>> = safeCall {
        val response = api.getMessages(chatId)
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải tin nhắn"))
        }
    }

    suspend fun sendMessage(chatId: Int, content: String): Result<Unit> = safeCall {
        val response = api.sendMessage(chatId, SendMessageRequest(content))
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Gửi tin nhắn thất bại"))
    }

    private suspend fun <T> safeCall(block: suspend () -> Result<T>): Result<T> {
        return try { block() } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }
}
