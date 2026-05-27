package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sportmanagement.manager.domain.model.ChatMessage
import com.sportmanagement.manager.domain.model.ConversationItem
import com.sportmanagement.manager.ui.state.MessagesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MessagesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onConversationClick(conversation: ConversationItem) {
        val markRead = _uiState.value.conversations.map { conv ->
            if (conv.id == conversation.id) conv.copy(unreadCount = 0) else conv
        }
        _uiState.value = _uiState.value.copy(
            selectedConversation = conversation,
            conversations = markRead
        )
    }

    fun onBackFromThread() {
        _uiState.value = _uiState.value.copy(
            selectedConversation = null,
            draftMessage = ""
        )
    }

    fun onDraftMessageChanged(text: String) {
        _uiState.value = _uiState.value.copy(draftMessage = text)
    }

    fun onSendMessage() {
        val conv = _uiState.value.selectedConversation ?: return
        val draft = _uiState.value.draftMessage.trim()
        if (draft.isBlank()) return

        val newMessage = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            content = draft,
            isFromManager = true,
            timestamp = "Vừa xong",
            isRead = false
        )
        val currentMessages = _uiState.value.chatMessages[conv.id] ?: emptyList()
        val updatedMessages = _uiState.value.chatMessages.toMutableMap()
        updatedMessages[conv.id] = currentMessages + newMessage

        val updatedConversations = _uiState.value.conversations.map { c ->
            if (c.id == conv.id) c.copy(lastMessage = draft, lastMessageTime = "Vừa xong") else c
        }

        _uiState.value = _uiState.value.copy(
            chatMessages = updatedMessages,
            conversations = updatedConversations,
            draftMessage = ""
        )
    }

    fun onReplyDraftChanged(reviewId: String, text: String) {
        val updated = _uiState.value.replyDrafts.toMutableMap()
        updated[reviewId] = text
        _uiState.value = _uiState.value.copy(replyDrafts = updated)
    }

    fun onSendReply(reviewId: String) {
        val draft = _uiState.value.replyDrafts[reviewId]?.trim() ?: return
        if (draft.isBlank()) return

        val updatedReviews = _uiState.value.reviews.map { review ->
            if (review.id == reviewId) review.copy(
                managerReply = draft,
                replyTimestamp = "Vừa xong"
            ) else review
        }
        val updatedDrafts = _uiState.value.replyDrafts.toMutableMap()
        updatedDrafts.remove(reviewId)

        _uiState.value = _uiState.value.copy(
            reviews = updatedReviews,
            replyDrafts = updatedDrafts
        )
    }
}
