package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import com.sportmanagement.manager.data.mapper.toChatMessage
import com.sportmanagement.manager.data.mapper.toConversationItem
import com.sportmanagement.manager.domain.model.ChatMessage
import com.sportmanagement.manager.domain.model.ConversationItem
import com.sportmanagement.manager.domain.model.MessageStatus
import com.sportmanagement.manager.ui.state.MessagesUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MessagesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    // One-shot event: navigate to this conversation from outside (e.g. booking detail → chat)
    private val _openChatEvent = Channel<ConversationItem>(Channel.BUFFERED)
    val openChatEvent: Flow<ConversationItem> = _openChatEvent.receiveAsFlow()

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            loadConversationsFromCache()
            loadConversations()
        }
    }

    // ── Conversations ─────────────────────────────────────────────────────────

    private suspend fun loadConversationsFromCache() {
        val cached = AppContainer.chatRepository.getCachedConversations()
        if (cached.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                conversations = cached.map { it.toConversationItem() }
            )
        }
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
        val chatId = conversation.id.toIntOrNull() ?: return
        viewModelScope.launch {
            AppContainer.chatRepository.markConversationAsRead(chatId)
        }
        loadMessages(chatId, showCacheFirst = true)
        startPolling(chatId)
    }

    // ── Polling ───────────────────────────────────────────────────────────────

    private fun startPolling(chatId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                silentRefreshMessages(chatId)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun silentRefreshMessages(chatId: Int) {
        val currentUserId = AppContainer.authRepository.getUserId()
        AppContainer.chatRepository.getMessages(chatId).onSuccess { dtos ->
            val freshMsgs = dtos.map { it.toChatMessage(currentUserId) }
            val chatKey = chatId.toString()
            val existing = _uiState.value.chatMessages[chatKey].orEmpty()
            // Keep SENDING and FAILED temp messages; replace confirmed messages with server data
            val tempMsgs = existing.filter { it.id.startsWith("temp_") }
            val merged = freshMsgs + tempMsgs
            val map = _uiState.value.chatMessages.toMutableMap()
            map[chatKey] = merged
            _uiState.value = _uiState.value.copy(chatMessages = map)
            // New messages from customer → refresh conversation list so it reorders to top
            val confirmedExisting = existing.count { !it.id.startsWith("temp_") }
            if (freshMsgs.size > confirmedExisting) {
                AppContainer.chatRepository.getChatList().onSuccess { convDtos ->
                    _uiState.value = _uiState.value.copy(
                        conversations = convDtos.map { it.toConversationItem() }
                    )
                }
            }
        }
    }

    // ── Messages ──────────────────────────────────────────────────────────────

    private fun loadMessages(chatId: Int, showCacheFirst: Boolean = false) {
        viewModelScope.launch {
            val currentUserId = AppContainer.authRepository.getUserId()
            val chatKey = chatId.toString()

            if (showCacheFirst) {
                val cached = AppContainer.chatRepository.getCachedMessages(chatId)
                if (cached.isNotEmpty()) {
                    val cachedMsgs = cached.map { it.toChatMessage(currentUserId) }
                    val map = _uiState.value.chatMessages.toMutableMap()
                    map[chatKey] = cachedMsgs
                    _uiState.value = _uiState.value.copy(
                        chatMessages = map,
                        loadingChatId = null
                    )
                }
            }

            AppContainer.chatRepository.getMessages(chatId).onSuccess { dtos ->
                val freshMsgs = dtos.map { it.toChatMessage(currentUserId) }
                val existing = _uiState.value.chatMessages[chatKey].orEmpty()
                val failedMsgs = existing.filter { it.id.startsWith("temp_") && it.status == MessageStatus.FAILED }
                val merged = freshMsgs + failedMsgs
                val map = _uiState.value.chatMessages.toMutableMap()
                map[chatKey] = merged
                _uiState.value = _uiState.value.copy(
                    chatMessages = map,
                    loadingChatId = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(loadingChatId = null)
            }
        }
    }

    fun onBackFromThread() {
        stopPolling()
        _uiState.value = _uiState.value.copy(
            selectedConversation = null,
            draftMessage = "",
            isSendingMessage = false
        )
    }

    fun onDraftMessageChanged(text: String) {
        _uiState.value = _uiState.value.copy(draftMessage = text)
    }

    fun onSendMessage() {
        val conv = _uiState.value.selectedConversation ?: return
        val draft = _uiState.value.draftMessage.trim()
        if (draft.isBlank() || _uiState.value.isSendingMessage) return
        val chatId = conv.id.toIntOrNull() ?: return
        val chatKey = conv.id

        val tempId = "temp_${System.currentTimeMillis()}"
        val tempMsg = ChatMessage(
            id = tempId,
            content = draft,
            isFromManager = true,
            timestamp = "Vừa xong",
            rawTimestamp = null,
            isRead = false,
            status = MessageStatus.SENDING
        )
        val currentMessages = _uiState.value.chatMessages[chatKey] ?: emptyList()
        val updatedMessages = _uiState.value.chatMessages.toMutableMap()
        updatedMessages[chatKey] = currentMessages + tempMsg

        val updatedConversations = buildList {
            add(conv.copy(lastMessage = draft, lastMessageTime = "Vừa xong"))
            addAll(_uiState.value.conversations.filter { it.id != conv.id })
        }
        _uiState.value = _uiState.value.copy(
            chatMessages = updatedMessages,
            conversations = updatedConversations,
            draftMessage = "",
            isSendingMessage = true
        )

        viewModelScope.launch {
            AppContainer.chatRepository.sendMessage(chatId, draft).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSendingMessage = false)
                    loadMessages(chatId)
                },
                onFailure = {
                    val currentMsgs = _uiState.value.chatMessages[chatKey].orEmpty()
                    val updatedMap = _uiState.value.chatMessages.toMutableMap()
                    updatedMap[chatKey] = currentMsgs.map { msg ->
                        if (msg.id == tempId) msg.copy(status = MessageStatus.FAILED) else msg
                    }
                    _uiState.value = _uiState.value.copy(
                        chatMessages = updatedMap,
                        isSendingMessage = false
                    )
                }
            )
        }
    }

    fun onRetryMessage(tempId: String) {
        val conv = _uiState.value.selectedConversation ?: return
        val chatId = conv.id.toIntOrNull() ?: return
        val chatKey = conv.id
        val messages = _uiState.value.chatMessages[chatKey] ?: return
        val failedMsg = messages.find { it.id == tempId } ?: return
        val content = failedMsg.content

        val newTempId = "temp_${System.currentTimeMillis()}"
        val retryMsg = failedMsg.copy(id = newTempId, status = MessageStatus.SENDING)
        val updatedMessages = _uiState.value.chatMessages.toMutableMap()
        updatedMessages[chatKey] = messages.filter { it.id != tempId } + retryMsg

        _uiState.value = _uiState.value.copy(
            chatMessages = updatedMessages,
            isSendingMessage = true
        )

        viewModelScope.launch {
            AppContainer.chatRepository.sendMessage(chatId, content).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSendingMessage = false)
                    loadMessages(chatId)
                },
                onFailure = {
                    val currentMsgs = _uiState.value.chatMessages[chatKey].orEmpty()
                    val updatedMap = _uiState.value.chatMessages.toMutableMap()
                    updatedMap[chatKey] = currentMsgs.map { msg ->
                        if (msg.id == newTempId) msg.copy(status = MessageStatus.FAILED) else msg
                    }
                    _uiState.value = _uiState.value.copy(
                        chatMessages = updatedMap,
                        isSendingMessage = false
                    )
                }
            )
        }
    }

    // ── Open / create chat with a user (from booking detail) ─────────────────

    fun openChatWithUser(userId: Int, customerName: String, customerPhone: String?) {
        _uiState.value = _uiState.value.copy(isStartingChat = true)
        viewModelScope.launch {
            AppContainer.chatRepository.getOrCreateChatWith(userId).fold(
                onSuccess = { chatDto ->
                    val conv = ConversationItem(
                        id = chatDto.chatId.toString(),
                        customerName = chatDto.customerName ?: customerName,
                        customerPhone = chatDto.customerPhone ?: customerPhone ?: "",
                        customerAvatarUrl = chatDto.customerAvatar,
                        isOnline = false,
                        lastMessage = chatDto.lastMessage ?: "",
                        lastMessageTime = "",
                        unreadCount = 0
                    )
                    _uiState.value = _uiState.value.copy(isStartingChat = false)
                    // Emit one-shot event; ManagerApp collects it and handles navigation
                    _openChatEvent.trySend(conv)
                    loadConversations()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isStartingChat = false, error = e.message)
                }
            )
        }
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

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
}
