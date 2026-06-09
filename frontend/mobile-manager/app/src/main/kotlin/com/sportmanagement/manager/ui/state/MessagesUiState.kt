package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.ChatMessage
import com.sportmanagement.manager.domain.model.ConversationItem
import com.sportmanagement.manager.domain.model.ReviewItem

data class MessagesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loadingChatId: String? = null,
    val searchQuery: String = "",
    val conversations: List<ConversationItem> = emptyList(),
    val reviews: List<ReviewItem> = emptyList(),
    val selectedConversation: ConversationItem? = null,
    val chatMessages: Map<String, List<ChatMessage>> = emptyMap(),
    val draftMessage: String = "",
    val averageRating: Float = 0f,
    val totalReviews: Int = 0,
    val replyDrafts: Map<String, String> = emptyMap()
) {
    val filteredConversations: List<ConversationItem>
        get() {
            val base = if (searchQuery.isBlank()) conversations
                       else conversations.filter {
                           it.customerName.contains(searchQuery, ignoreCase = true) ||
                               it.customerPhone.contains(searchQuery)
                       }
            // Unread conversations float to top; within each group keep original order (backend: updated_at DESC)
            return base.sortedWith(compareByDescending { it.unreadCount > 0 })
        }

    val unreadCount: Int get() = conversations.sumOf { it.unreadCount }
}
