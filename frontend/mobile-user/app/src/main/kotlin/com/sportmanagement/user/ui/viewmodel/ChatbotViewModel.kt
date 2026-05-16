package com.sportmanagement.user.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.user.data.repository.ChatbotRepositoryImpl
import com.sportmanagement.user.domain.model.ChatbotDeliveryState
import com.sportmanagement.user.domain.model.ChatbotMessage
import com.sportmanagement.user.domain.model.ChatbotSender
import com.sportmanagement.user.domain.repository.ChatbotRepository
import com.sportmanagement.user.ui.state.ChatbotUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class ChatbotViewModel(
    private val repository: ChatbotRepository = ChatbotRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState

    fun setWidgetEnabled(enabled: Boolean) {
        _uiState.update { current ->
            current.copy(
                isWidgetEnabled = enabled,
                isWindowOpen = if (enabled) current.isWindowOpen else false,
                errorMessage = if (enabled) current.errorMessage else null
            )
        }
    }

    fun toggleWindow() {
        val current = _uiState.value
        if (!current.isWidgetEnabled) return

        _uiState.update {
            it.copy(
                isWindowOpen = !it.isWindowOpen,
                errorMessage = null
            )
        }
    }

    fun closeWindow() {
        _uiState.update { it.copy(isWindowOpen = false, errorMessage = null) }
    }

    fun onDraftMessageChange(value: String) {
        _uiState.update { current ->
            current.copy(
                draftMessage = value,
                errorMessage = if (current.errorMessage != null && value.isNotBlank()) null else current.errorMessage
            )
        }
    }

    fun onButtonAnchorChanged(x: Float, y: Float) {
        _uiState.update {
            it.copy(
                buttonAnchorX = x.coerceIn(0f, 1f),
                buttonAnchorY = y.coerceIn(0f, 1f)
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun sendDraftMessage() {
        val current = _uiState.value
        val outgoingMessage = current.draftMessage.trim()
        if (outgoingMessage.isBlank() || current.isLoading || current.isTyping || !current.isWidgetEnabled) return

        val history = current.messages.deliveredMessages()
        val userMessage = ChatbotMessage(
            id = UUID.randomUUID().toString(),
            text = outgoingMessage,
            sender = ChatbotSender.USER
        )

        _uiState.update {
            it.copy(
                draftMessage = "",
                isLoading = true,
                isWindowOpen = true,
                errorMessage = null,
                messages = it.messages + userMessage
            )
        }

        requestReply(
            outgoingMessage = outgoingMessage,
            history = history,
            userMessageId = userMessage.id,
            shouldRestoreFailedMessage = true
        )
    }

    fun retryMessage(messageId: String) {
        val current = _uiState.value
        if (current.isLoading || current.isTyping) return

        val messageIndex = current.messages.indexOfFirst { it.id == messageId }
        if (messageIndex == -1) return

        val failedMessage = current.messages[messageIndex]
        if (failedMessage.sender != ChatbotSender.USER ||
            failedMessage.deliveryState != ChatbotDeliveryState.FAILED
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        requestReply(
            outgoingMessage = failedMessage.text,
            history = current.messages
                .take(messageIndex)
                .deliveredMessages(),
            userMessageId = failedMessage.id,
            shouldRestoreFailedMessage = false
        )
    }

    private fun requestReply(
        outgoingMessage: String,
        history: List<ChatbotMessage>,
        userMessageId: String,
        shouldRestoreFailedMessage: Boolean
    ) {
        viewModelScope.launch {
            try {
                val reply = repository.sendMessage(
                    message = outgoingMessage,
                    conversationHistory = history
                )
                showTypewriterReply(
                    userMessageId = userMessageId,
                    reply = reply
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                handleReplyFailure(
                    userMessageId = userMessageId,
                    shouldRestoreFailedMessage = shouldRestoreFailedMessage,
                    message = exception.message ?: "Không thể kết nối chatbot lúc này"
                )
            } catch (exception: Exception) {
                handleReplyFailure(
                    userMessageId = userMessageId,
                    shouldRestoreFailedMessage = shouldRestoreFailedMessage,
                    message = "Đã xảy ra lỗi khi gửi tin nhắn"
                )
            }
        }
    }

    private fun handleReplyFailure(
        userMessageId: String,
        shouldRestoreFailedMessage: Boolean,
        message: String
    ) {
        _uiState.update { current ->
            current.copy(
                isLoading = false,
                isTyping = false,
                errorMessage = message,
                messages = current.messages.map { chatMessage ->
                    if (shouldRestoreFailedMessage && chatMessage.id == userMessageId) {
                        chatMessage.copy(deliveryState = ChatbotDeliveryState.FAILED)
                    } else {
                        chatMessage
                    }
                }
            )
        }
    }

    private suspend fun showTypewriterReply(
        userMessageId: String,
        reply: String
    ) {
        val botMessageId = UUID.randomUUID().toString()

        _uiState.update { current ->
            current.copy(
                isLoading = false,
                isTyping = true,
                errorMessage = null,
                messages = current.messages
                    .map { message ->
                        if (message.id == userMessageId) {
                            message.copy(deliveryState = ChatbotDeliveryState.DELIVERED)
                        } else {
                            message
                        }
                    } + ChatbotMessage(
                    id = botMessageId,
                    text = "",
                    sender = ChatbotSender.BOT
                )
            )
        }

        val builder = StringBuilder()
        reply.forEachIndexed { index, character ->
            builder.append(character)
            val nextText = builder.toString()

            _uiState.update { current ->
                current.copy(
                    messages = current.messages.map { message ->
                        if (message.id == botMessageId) {
                            message.copy(text = nextText)
                        } else {
                            message
                        }
                    }
                )
            }

            if (index < reply.lastIndex) {
                delay(character.typewriterDelayMs())
            }
        }

        _uiState.update { current ->
            current.copy(isTyping = false)
        }
    }
}

private fun List<ChatbotMessage>.deliveredMessages(): List<ChatbotMessage> =
    filter { it.deliveryState == ChatbotDeliveryState.DELIVERED }

private fun Char.typewriterDelayMs(): Long =
    when (this) {
        '.', '!', '?', '\n' -> 90L
        ',', ';', ':' -> 55L
        ' ' -> 28L
        else -> 20L
    }
