package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.data.mapper.toChatMessage
import com.sportmanagement.manager.data.mapper.toConversationItem
import com.sportmanagement.manager.domain.model.ChatMessage
import com.sportmanagement.manager.domain.model.ConversationItem
import com.sportmanagement.manager.ui.state.MessagesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            AppContainer.chatRepository.getChatList().fold(
                onSuccess = { dtos ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        conversations = dtos.map { it.toConversationItem() }
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onConversationClick(conversation: ConversationItem) {
        val markRead = _uiState.value.conversations.map { conv ->
            if (conv.id == conversation.id) conv.copy(unreadCount = 0) else conv
        }
        _uiState.value = _uiState.value.copy(
            selectedConversation = conversation,
            conversations = markRead,
            loadingChatId = conversation.id
        )
        loadMessages(conversation.id.toIntOrNull() ?: return)
    }

    private fun loadMessages(chatId: Int) {
        viewModelScope.launch {
            val currentUserId = AppContainer.authRepository.getUserId()
            val chatKey = chatId.toString()
            AppContainer.chatRepository.getMessages(chatId).onSuccess { dtos ->
                val freshMessages = dtos.map { it.toChatMessage(currentUserId) }
                val existingMessages = _uiState.value.chatMessages[chatKey].orEmpty()
                val mergedMessages = mergeMessages(existingMessages, freshMessages)
                val updatedMap = _uiState.value.chatMessages.toMutableMap()
                updatedMap[chatKey] = mergedMessages
                _uiState.value = _uiState.value.copy(
                    chatMessages = updatedMap,
                    loadingChatId = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(loadingChatId = null)
            }
        }
    }

    fun onBackFromThread() {
        _uiState.value = _uiState.value.copy(selectedConversation = null, draftMessage = "")
    }

    fun onDraftMessageChanged(text: String) {
        _uiState.value = _uiState.value.copy(draftMessage = text)
    }

    fun onSendMessage() {
        val conv = _uiState.value.selectedConversation ?: return
        val draft = _uiState.value.draftMessage.trim()
        if (draft.isBlank()) return
        val chatId = conv.id.toIntOrNull() ?: return

        // Optimistic update
        val tempMsg = ChatMessage(
            id = "temp_${System.currentTimeMillis()}",
            content = draft,
            isFromManager = true,
            timestamp = "Vừa xong",
            isRead = false
        )
        val currentMessages = _uiState.value.chatMessages[conv.id] ?: emptyList()
        val updatedMap = _uiState.value.chatMessages.toMutableMap()
        updatedMap[conv.id] = currentMessages + tempMsg

        val updatedConversations = buildList {
            add(conv.copy(lastMessage = draft, lastMessageTime = "Vừa xong"))
            addAll(_uiState.value.conversations.filter { it.id != conv.id })
        }
        _uiState.value = _uiState.value.copy(chatMessages = updatedMap, conversations = updatedConversations, draftMessage = "")

        viewModelScope.launch {
            AppContainer.chatRepository.sendMessage(chatId, draft).onFailure {
                loadMessages(chatId)
                return@launch
            }
            loadMessages(chatId)
        }
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
            if (review.id == reviewId) review.copy(managerReply = draft, replyTimestamp = "Vừa xong") else review
        }
        val updatedDrafts = _uiState.value.replyDrafts.toMutableMap().also { it.remove(reviewId) }
        _uiState.value = _uiState.value.copy(reviews = updatedReviews, replyDrafts = updatedDrafts)
    }

    private fun mergeMessages(
        existingMessages: List<ChatMessage>,
        freshMessages: List<ChatMessage>
    ): List<ChatMessage> {
        if (existingMessages.isEmpty()) return freshMessages
        if (freshMessages.isEmpty()) return existingMessages

        val merged = LinkedHashMap<String, ChatMessage>()
        existingMessages.forEach { merged[it.id] = it }
        freshMessages.forEach { merged[it.id] = it }
        return merged.values.toList()
    }
}
