package com.sportmanagement.user.domain.repository

import com.sportmanagement.user.domain.model.ChatbotMessage

interface ChatbotRepository {
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<ChatbotMessage>
    ): String
}
