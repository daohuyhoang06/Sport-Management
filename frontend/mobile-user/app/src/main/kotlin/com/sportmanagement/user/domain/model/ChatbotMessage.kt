package com.sportmanagement.user.domain.model

enum class ChatbotSender {
    USER,
    BOT
}

enum class ChatbotDeliveryState {
    DELIVERED,
    FAILED
}

data class ChatbotMessage(
    val id: String,
    val text: String,
    val sender: ChatbotSender,
    val deliveryState: ChatbotDeliveryState = ChatbotDeliveryState.DELIVERED
)
