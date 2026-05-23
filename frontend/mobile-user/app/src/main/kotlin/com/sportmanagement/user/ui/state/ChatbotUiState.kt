package com.sportmanagement.user.ui.state

import com.sportmanagement.user.domain.model.ChatbotMessage

data class ChatbotUiState(
    val isWidgetEnabled: Boolean = true,
    val isWindowOpen: Boolean = false,
    val draftMessage: String = "",
    val messages: List<ChatbotMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val errorMessage: String? = null,
    val buttonAnchorX: Float = 1f,
    val buttonAnchorY: Float = 0.86f
)
