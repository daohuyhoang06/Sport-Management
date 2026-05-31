package com.sportmanagement.user.data.repository

import com.sportmanagement.user.data.remote.api.ChatbotApi
import com.sportmanagement.user.domain.model.ChatbotDeliveryState
import com.sportmanagement.user.domain.model.ChatbotMessage
import com.sportmanagement.user.domain.repository.ChatbotRepository

class ChatbotRepositoryImpl : ChatbotRepository {
    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<ChatbotMessage>
    ): String {
        return ChatbotApi.sendMessage(
            message = message,
            conversationHistory = conversationHistory.filter {
                it.deliveryState == ChatbotDeliveryState.DELIVERED
            }
        )
    }
}
