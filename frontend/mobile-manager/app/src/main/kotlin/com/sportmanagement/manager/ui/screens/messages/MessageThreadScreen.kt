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
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import java.util.TimeZone

// ── Colors ────────────────────────────────────────────────────────────────────

private val ChatBg         = Color(0xFFF0F2F5)
private val CustomerBubble = Color.White
private val TimestampColor = Color(0xFF9EA3AE)

// ── Data model ────────────────────────────────────────────────────────────────

private data class DisplayMsg(
    val msg: ChatMessage,
    val isFirst: Boolean,   // first in consecutive same-sender group → extra top gap
    val isLast: Boolean,    // last in group → show avatar + timestamp
    val dateSep: String?    // non-null → draw date chip before this bubble
)

// ── Screen ────────────────────────────────────────────────────────────────────

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
    val displayItems = remember(messages) { buildDisplayMsgs(messages) }

    // Auto-scroll to the newest message whenever the list grows
    LaunchedEffect(displayItems.size) {
        if (displayItems.isNotEmpty()) {
            listState.animateScrollToItem(displayItems.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatBg)
            .imePadding()
    ) {
        // ── AppBar ────────────────────────────────────────────────────────────
        val context = LocalContext.current
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarCircle(
                        name = conversation.customerName,
                        url  = conversation.customerAvatarUrl,
                        size = 36,
                        isOnline = conversation.isOnline
                    )
                    Column {
                        Text(
                            text  = conversation.customerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text  = conversation.customerPhone.ifBlank { "Khách hàng" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                }
            },
            actions = {
                if (conversation.customerPhone.isNotBlank()) {
                    IconButton(onClick = {
                        val phone = conversation.customerPhone.trim()
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Gọi điện",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            modifier = Modifier.shadow(3.dp)
        )

        // ── Loading indicator (first load only) ───────────────────────────────
        if (isLoading && displayItems.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        // ── Message list ─────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(displayItems, key = { it.msg.id }) { item ->
                BubbleItem(
                    item = item,
                    customerName = conversation.customerName,
                    customerAvatarUrl = conversation.customerAvatarUrl,
                    onRetry = { onRetryMessage(item.msg.id) }
                )
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────────
        InputBar(
            value    = draftMessage,
            isSending = isSendingMessage,
            onValueChange = onDraftChanged,
            onSend   = onSend
        )
    }
}

// ── BubbleItem ─────────────────────────────────────────────────────────────────

@Composable
private fun BubbleItem(
    item: DisplayMsg,
    customerName: String,
    customerAvatarUrl: String?,
    onRetry: () -> Unit
) {
    val msg      = item.msg
    val isMe     = msg.isFromManager
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.72f).dp

    // Date separator
    if (item.dateSep != null) {
        DateChip(label = item.dateSep)
    }

    // Top gap: bigger between groups, tiny within group
    val topPad = if (item.isFirst) 10.dp else 2.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPad),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Customer avatar space (always reserve 38dp for alignment)
        if (!isMe) {
            Box(Modifier.size(30.dp).padding(bottom = 2.dp)) {
                if (item.isLast) {
                    AvatarCircle(customerName, customerAvatarUrl, size = 28)
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        // Bubble + metadata column
        Column(
            modifier = Modifier.widthIn(max = maxWidth),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            // ── Bubble ──
            val shape = if (isMe) {
                RoundedCornerShape(18.dp, 18.dp, if (item.isLast) 4.dp else 18.dp, 18.dp)
            } else {
                RoundedCornerShape(18.dp, 18.dp, 18.dp, if (item.isLast) 4.dp else 18.dp)
            }

            val bubbleBg = when {
                isMe && msg.status == MessageStatus.FAILED -> Color(0xFFFFE4E6)
                isMe                                       -> MaterialTheme.colorScheme.primary
                else                                       -> CustomerBubble
            }

            Box(
                modifier = Modifier
                    .then(
                        if (!isMe) Modifier.shadow(1.dp, shape, clip = false)
                        else Modifier
                    )
                    .clip(shape)
                    .background(bubbleBg)
                    .padding(horizontal = 13.dp, vertical = 9.dp)
            ) {
                Text(
                    text = msg.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isMe && msg.status == MessageStatus.FAILED -> Color(0xFFBE123C)
                        isMe                                       -> Color.White
                        else                                       -> Color(0xFF1A1A2E)
                    },
                    lineHeight = 20.sp
                )
            }

            // ── Status / timestamp row ──
            if (item.isLast || msg.status != MessageStatus.SENT) {
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    when (msg.status) {
                        MessageStatus.SENDING -> {
                            CircularProgressIndicator(
                                modifier  = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = TimestampColor
                            )
                            Text(msg.timestamp, fontSize = 10.sp, color = TimestampColor)
                        }
                        MessageStatus.FAILED -> {
                            Row(
                                modifier = Modifier.clickable { onRetry() },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = Color(0xFFE11D48)
                                )
                                Text(
                                    "Gửi thất bại · Thử lại",
                                    fontSize = 10.sp,
                                    color = Color(0xFFE11D48),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = Color(0xFFE11D48)
                                )
                            }
                        }
                        MessageStatus.SENT -> {
                            Text(item.timeLabel, fontSize = 10.sp, color = TimestampColor)
                            if (isMe) {
                                Icon(
                                    imageVector = if (msg.isRead) Icons.Filled.DoneAll else Icons.Filled.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (msg.isRead) MaterialTheme.colorScheme.primary else TimestampColor
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isMe) Spacer(Modifier.width(4.dp))
    }
}

// ── Date chip ─────────────────────────────────────────────────────────────────

@Composable
private fun DateChip(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFDDE1E9))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Avatar ────────────────────────────────────────────────────────────────────

@Composable
private fun AvatarCircle(
    name: String,
    url: String?,
    size: Int,
    isOnline: Boolean = false
) {
    Box(modifier = Modifier.size(size.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            if (!url.isNullOrBlank()) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    text = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K",
                    fontSize = (size * 0.38f).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((size * 0.27f).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}

// ── Input bar ─────────────────────────────────────────────────────────────────

@Composable
private fun InputBar(
    value: String,
    isSending: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val canSend = value.isNotBlank() && !isSending

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        // Top separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE8EBF0))
        )
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
                Text("Nhắn tin...", color = Color(0xFF9EA3AE), fontSize = 14.sp)
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = Color(0xFFF0F2F5),
                unfocusedContainerColor = Color(0xFFF0F2F5),
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedTextColor        = Color(0xFF1A1A2E),
                unfocusedTextColor      = Color(0xFF1A1A2E)
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
            maxLines = 5,
            enabled = !isSending
        )

        // Send button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (canSend) MaterialTheme.colorScheme.primary
                    else Color(0xFFE4E7ED)
                )
                .then(if (canSend) Modifier.clickable { onSend() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (isSending && value.isBlank()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Gửi",
                    tint = if (canSend) Color.White else Color(0xFFB0B7C3),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }   // Row
    }   // Column
}

// ── Build display items ───────────────────────────────────────────────────────

private fun buildDisplayMsgs(messages: List<ChatMessage>): List<DisplayMsg> {
    if (messages.isEmpty()) return emptyList()

    // Primary sort: message_id (auto-increment int) for server messages → always chronological.
    // Temp messages use embedded System.currentTimeMillis() which is ~1.7 trillion,
    // far above any real message_id, so they always appear last.
    val sorted = messages.sortedBy { it.sortIndex() }

    val result = mutableListOf<DisplayMsg>()
    var lastDateStr = ""

    for (i in sorted.indices) {
        val msg  = sorted[i]
        val prev = sorted.getOrNull(i - 1)
        val next = sorted.getOrNull(i + 1)

        val isFirst = prev == null || prev.isFromManager != msg.isFromManager
        val isLast  = next == null || next.isFromManager != msg.isFromManager

        val epoch   = msg.timestampEpoch()
        val dateStr = epoch?.let { epochToDate(it) } ?: ""

        val dateSep: String? = if (dateStr.isNotBlank() && dateStr != lastDateStr) {
            lastDateStr = dateStr
            dateLabelFor(dateStr)
        } else null

        result.add(DisplayMsg(msg, isFirst, isLast, dateSep))
    }

    return result
}

// ── Helpers ───────────────────────────────────────────────────────────────────

// Sort index: use numeric message_id for server messages (auto-increment = insertion order).
// Temp IDs embed System.currentTimeMillis() (~1.7 trillion) → always > any real message_id.
private fun ChatMessage.sortIndex(): Long = when {
    id.startsWith("temp_") -> id.removePrefix("temp_").toLongOrNull() ?: Long.MAX_VALUE
    else                   -> id.toLongOrNull() ?: 0L
}

// Parse raw timestamp for DISPLAY only (date chips, time labels).
// Backend returns ISO UTC strings (e.g. "2024-01-15T03:00:00.000Z").
// Must parse as UTC then format in device local timezone.
private fun ChatMessage.timestampEpoch(): Long? {
    val raw = rawTimestamp?.takeIf { it.isNotBlank() } ?: return null
    val utc = TimeZone.getTimeZone("UTC")
    return runCatching {
        when {
            raw.endsWith("Z") && raw.contains('.') ->
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    .also { it.timeZone = utc }.parse(raw)?.time
            raw.endsWith("Z") ->
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .also { it.timeZone = utc }.parse(raw)?.time
            raw.contains('T') ->
                // ISO without Z — no timezone info, treat as local
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(raw.take(19))?.time
            else ->
                // MySQL "YYYY-MM-DD HH:MM:SS" — treat as local
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(raw.take(19))?.time
        }
    }.getOrNull()?.takeIf { it > 0L }
}

private val DisplayMsg.timeLabel: String
    get() = msg.timestampEpoch()?.let { epochToTime(it) } ?: msg.timestamp

private fun epochToDate(epoch: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(epoch))

private fun epochToTime(epoch: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epoch))

private fun dateLabelFor(dateStr: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today     = sdf.format(Date())
    val yesterday = sdf.format(Date(System.currentTimeMillis() - 86_400_000L))
    return when (dateStr) {
        today     -> "Hôm nay"
        yesterday -> "Hôm qua"
        else -> runCatching {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(sdf.parse(dateStr)!!)
        }.getOrDefault(dateStr)
    }
}
