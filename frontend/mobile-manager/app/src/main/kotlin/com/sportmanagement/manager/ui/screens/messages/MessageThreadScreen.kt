package com.sportmanagement.manager.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sportmanagement.manager.domain.model.ChatMessage
import com.sportmanagement.manager.domain.model.ConversationItem
import com.sportmanagement.manager.domain.model.MessageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class ListItem {
    data class DateSep(val label: String) : ListItem()
    data class Msg(val message: ChatMessage) : ListItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageThreadScreen(
    conversation: ConversationItem,
    messages: List<ChatMessage>,
    draftMessage: String,
    isLoading: Boolean = false,
    isSendingMessage: Boolean = false,
    onBackClick: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onRetryMessage: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val prevSize = remember { mutableIntStateOf(0) }

    val listItems = remember(messages) { buildListItems(messages) }

    LaunchedEffect(messages.size) {
        if (listItems.isNotEmpty()) {
            val isNewMessage = messages.size > prevSize.intValue
            if (isNewMessage) {
                listState.animateScrollToItem(listItems.lastIndex)
            } else {
                listState.scrollToItem(listItems.lastIndex)
            }
            prevSize.intValue = messages.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.size(38.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!conversation.customerAvatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = conversation.customerAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = conversation.customerName.first().uppercaseChar().toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (conversation.isOnline) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = conversation.customerName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (conversation.isOnline) "Đang hoạt động"
                                   else conversation.customerPhone.ifBlank { "Khách hàng" },
                            fontSize = 11.sp,
                            color = if (conversation.isOnline) Color(0xFF22C55E)
                                    else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Gọi điện",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Thêm",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            modifier = Modifier.shadow(4.dp)
        )

        if (conversation.totalBookings > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Khách đặt sân ${conversation.totalBookings} lần  •  ${conversation.customerPhone}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (isLoading && messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(listItems, key = { item ->
                when (item) {
                    is ListItem.DateSep -> "sep_${item.label}"
                    is ListItem.Msg     -> item.message.id
                }
            }) { item ->
                when (item) {
                    is ListItem.DateSep -> DateSeparator(item.label)
                    is ListItem.Msg     -> MessageBubble(
                        message = item.message,
                        customerName = conversation.customerName,
                        customerAvatarUrl = conversation.customerAvatarUrl,
                        onRetry = { onRetryMessage(item.message.id) }
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        MessageInputBar(
            value = draftMessage,
            isSending = isSendingMessage,
            onValueChange = onDraftChanged,
            onSend = onSend
        )
    }
}

@Composable
private fun DateSeparator(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    customerName: String,
    customerAvatarUrl: String?,
    onRetry: () -> Unit
) {
    val isManager = message.isFromManager

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isManager) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isManager) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!customerAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = customerAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text = customerName.first().uppercaseChar().toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        val maxBubbleWidth = (LocalConfiguration.current.screenWidthDp * 0.72f).dp
        Column(
            modifier = Modifier.widthIn(max = maxBubbleWidth),
            horizontalAlignment = if (isManager) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isManager) 16.dp else 4.dp,
                            bottomEnd = if (isManager) 4.dp else 16.dp
                        )
                    )
                    .background(
                        when {
                            isManager && message.status == MessageStatus.FAILED -> Color(0xFFFFE4E6)
                            isManager -> MaterialTheme.colorScheme.primary
                            else -> Color.White
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isManager && message.status == MessageStatus.FAILED -> Color(0xFFBE123C)
                        isManager -> Color.White
                        else -> MaterialTheme.colorScheme.onBackground
                    }
                )
            }
            Spacer(Modifier.height(2.dp))
            if (isManager) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = message.timestamp,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    when (message.status) {
                        MessageStatus.SENDING -> CircularProgressIndicator(
                            modifier = Modifier.size(11.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        MessageStatus.SENT -> Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        MessageStatus.FAILED -> Row(
                            modifier = Modifier.clickable { onRetry() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFFE11D48)
                            )
                            Text(
                                text = "Thử lại",
                                fontSize = 10.sp,
                                color = Color(0xFFE11D48),
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = Color(0xFFE11D48)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        if (isManager) {
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun MessageInputBar(
    value: String,
    isSending: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val canSend = value.isNotBlank() && !isSending
    Card(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Nhập tin nhắn...", color = MaterialTheme.colorScheme.outline)
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                maxLines = 4,
                enabled = !isSending
            )
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSend) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSending && value.isBlank()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    IconButton(onClick = { if (canSend) onSend() }) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Gửi",
                            tint = if (canSend) Color.White else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun buildListItems(messages: List<ChatMessage>): List<ListItem> = buildList {
    var lastDate = ""
    messages.forEach { msg ->
        val dateLabel = msg.rawTimestamp?.let { formatDateSeparator(it) } ?: ""
        if (dateLabel.isNotBlank() && dateLabel != lastDate) {
            add(ListItem.DateSep(dateLabel))
            lastDate = dateLabel
        }
        add(ListItem.Msg(msg))
    }
}

private val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val displaySdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

private fun formatDateSeparator(rawIso: String): String {
    return runCatching {
        val dateStr = rawIso.take(10)
        val today = dateSdf.format(Date())
        val yesterday = dateSdf.format(Date(System.currentTimeMillis() - 86_400_000L))
        when (dateStr) {
            today     -> "Hôm nay"
            yesterday -> "Hôm qua"
            else      -> displaySdf.format(dateSdf.parse(dateStr) ?: return@runCatching dateStr)
        }
    }.getOrDefault("")
}
