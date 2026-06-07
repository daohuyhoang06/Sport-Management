package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.ui.components.booking.bookingCardTitleStyle
import com.sportmanagement.user.ui.components.booking.bookingHelperTextStyle
import com.sportmanagement.user.ui.components.booking.bookingInfoValueStyle
import com.sportmanagement.user.ui.components.booking.bookingPageTitleStyle
import com.sportmanagement.user.ui.theme.AppHeaderGradientEnd
import com.sportmanagement.user.ui.theme.AppHeaderGradientStart
import com.sportmanagement.user.ui.theme.AppScreenHorizontalPadding

@Composable
fun BookingHistoryScreen(
    padding: PaddingValues,
    sections: List<NotificationSectionData>,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onBookingSelected: (bookingId: Int, notificationId: Int?) -> Unit
) {
    val bookingItems = sections
        .flatMap { it.items }
        .filter { it.category == InboxCategoryType.Booking }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BookingHistoryTopBar(
                onBackClick = onBackClick,
                onRefreshClick = onRefresh
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null && bookingItems.isEmpty() -> {
                        EmptyBookingHistoryState(
                            title = errorMessage,
                            body = "Kéo để tải lại danh sách đặt sân gần đây."
                        )
                    }

                    bookingItems.isEmpty() -> {
                        EmptyBookingHistoryState(
                            title = "Chưa có lịch sử đặt sân",
                            body = "Các booking đã xác nhận sẽ xuất hiện ở đây."
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = AppScreenHorizontalPadding,
                                end = AppScreenHorizontalPadding,
                                top = 8.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                if (errorMessage != null) {
                                    HistoryNoticeCard(message = errorMessage)
                                }
                            }

                            items(
                                items = bookingItems,
                                key = { item -> item.id ?: item.bookingId ?: item.title }
                            ) { item ->
                                BookingHistoryCard(
                                    item = item,
                                    onClick = {
                                        val bookingId = item.bookingId ?: item.bookingInfo?.bookingId
                                        if (bookingId != null) {
                                            onBookingSelected(
                                                bookingId,
                                                item.bookingInfo?.notificationId ?: item.id
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingHistoryTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(AppHeaderGradientStart, AppHeaderGradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = AppScreenHorizontalPadding, vertical = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = "Lịch sử đặt sân",
                    style = bookingPageTitleStyle(),
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onPrimary
                )

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingHistoryCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val bookingInfo = item.bookingInfo
    val fieldName = resolveFieldName(item, bookingInfo)
    val dateLabel = resolveDateLabel(item, bookingInfo)
    val timeRange = resolveTimeRange(item, bookingInfo)
    val statusText = bookingInfo?.statusLabel?.takeIf { it.isNotBlank() } ?: "Đã đặt"
    val amountText = bookingInfo?.totalAmount?.takeIf { it.isNotBlank() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = fieldName,
                    style = bookingCardTitleStyle(),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))
                HistoryStatusChip(text = statusText)
            }

            Spacer(modifier = Modifier.height(12.dp))

            HistoryDateRow(
                icon = Icons.Outlined.CalendarMonth,
                label = "Ngày",
                value = dateLabel
            )
            Spacer(modifier = Modifier.height(8.dp))
            HistorySlotSection(
                icon = Icons.Outlined.Schedule,
                label = "Khung giờ",
                value = timeRange
            )

            if (amountText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = amountText,
                        style = bookingInfoValueStyle(),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryDateRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HistorySlotSection(
    icon: ImageVector,
    label: String,
    value: String
) {
    val slotLines = splitSlotLines(value)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (slotLines.isEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    slotLines.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun splitSlotLines(value: String): List<String> {
    if (value.isBlank()) return emptyList()

    return value
        .replace("\r\n", "\n")
        .split('\n', '|')
        .map { it.trim() }
        .flatMap { part ->
            if (part.contains("•")) {
                part.split("•").map { it.trim() }.filter { it.isNotBlank() }
            } else {
                listOf(part)
            }
        }
        .map { it.replace(Regex("""\s+"""), " ").trim() }
        .filter { it.isNotBlank() }
}

private fun resolveFieldName(item: NotificationItem, bookingInfo: BookingInfo?): String {
    bookingInfo?.fieldName?.takeIf { value ->
        value.isNotBlank() && !value.contains("đặt sân", ignoreCase = true)
    }?.let { return it }

    return extractBookingHeadline(item).ifBlank { "Sân đặt" }
}

private fun resolveDateLabel(item: NotificationItem, bookingInfo: BookingInfo?): String {
    bookingInfo?.dateLabel?.let { rawDate ->
        extractDateToken(rawDate)?.let { return it }
        if (rawDate.matches(Regex("""\d{1,2}/\d{1,2}/\d{4}"""))) return rawDate
    }

    listOf(item.subtitle, item.longDetail)
        .forEach { text ->
            extractDateToken(text)?.let { return it }
        }

    return ""
}

private fun resolveTimeRange(item: NotificationItem, bookingInfo: BookingInfo?): String {
    bookingInfo?.timeRange?.takeIf { isValidTimeRange(it) }?.let { return it }

    listOf(item.subtitle, item.longDetail).forEach { text ->
        extractTimeRangeToken(text)?.let { return it }
    }

    return ""
}

private fun extractBookingHeadline(item: NotificationItem): String {
    val fromDetail = extractBookingDetailText(item)
        .substringBefore("•")
        .substringBefore(" - ")
        .trim()
    if (fromDetail.isNotBlank() && !fromDetail.contains("đặt sân", ignoreCase = true)) {
        return fromDetail
    }

    val fromSubtitle = item.subtitle
        .substringBefore("•")
        .substringBefore(" - ")
        .trim()
    if (fromSubtitle.isNotBlank() && !fromSubtitle.contains("đặt sân", ignoreCase = true)) {
        return fromSubtitle
    }

    return item.title.takeIf { it.isNotBlank() && !it.contains("đặt sân", ignoreCase = true) }.orEmpty()
}

private fun extractBookingDetailText(item: NotificationItem): String {
    return sequenceOf(item.detail, item.subtitle, item.longDetail)
        .firstOrNull { it.isNotBlank() && !it.contains("đặt sân thành công", ignoreCase = true) }
        .orEmpty()
}

private fun extractDateToken(text: String): String? {
    if (text.isBlank()) return null
    val match = Regex("""(\d{1,2}/\d{1,2}/\d{4})""").find(text)
    return match?.groupValues?.getOrNull(1)
}

private fun extractTimeRangeToken(text: String): String? {
    if (text.isBlank()) return null
    val match = Regex("""(\d{1,2}:\d{2}\s*-\s*\d{1,2}:\d{2})""").find(text)
    return match?.groupValues?.getOrNull(1)?.replace(" ", "")
}

private fun isValidTimeRange(text: String): Boolean {
    return Regex("""\d{1,2}:\d{2}\s*-\s*\d{1,2}:\d{2}""").containsMatchIn(text)
}

@Composable
private fun HistoryStatusChip(text: String) {
    val backgroundColor = when {
        text.contains("xác nhận", ignoreCase = true) -> Color(0xFFEFF6FF)
        text.contains("hoàn thành", ignoreCase = true) -> Color(0xFFEFFDF5)
        text.contains("đã thanh toán", ignoreCase = true) -> Color(0xFFF0FDF4)
        text.contains("đã huỷ", ignoreCase = true) || text.contains("đã hủy", ignoreCase = true) -> Color(0xFFFEF2F2)
        else -> Color(0xFFF8FAFC)
    }
    val contentColor = when {
        text.contains("xác nhận", ignoreCase = true) -> Color(0xFF2563EB)
        text.contains("hoàn thành", ignoreCase = true) -> Color(0xFF16A34A)
        text.contains("đã thanh toán", ignoreCase = true) -> Color(0xFF15803D)
        text.contains("đã huỷ", ignoreCase = true) || text.contains("đã hủy", ignoreCase = true) -> Color(0xFFDC2626)
        else -> Color(0xFF475569)
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.16f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = bookingHelperTextStyle(),
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun EmptyBookingHistoryState(
    title: String,
    body: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.EventAvailable,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = bookingCardTitleStyle(),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = bookingHelperTextStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistoryNoticeCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = bookingHelperTextStyle(),
            color = Color(0xFFB45309)
        )
    }
}
