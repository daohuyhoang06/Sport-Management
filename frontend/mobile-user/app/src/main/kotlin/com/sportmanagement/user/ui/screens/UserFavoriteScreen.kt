package com.sportmanagement.user.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.sportmanagement.user.R
import com.sportmanagement.user.ui.components.AppRotatingLoadingIndicator
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppFieldHorizontalPadding
import com.sportmanagement.user.ui.theme.AppHeaderGradientEnd
import com.sportmanagement.user.ui.theme.AppHeaderGradientStart
import com.sportmanagement.user.ui.theme.AppInputCornerRadius
import com.sportmanagement.user.ui.theme.AppPillCornerRadius
import com.sportmanagement.user.ui.theme.AppScreenHorizontalPadding
import com.sportmanagement.user.ui.theme.AppSheetTopCornerRadius
import com.sportmanagement.user.ui.theme.responsiveSharedTitleStyle
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

private val InboxSectionGap = AppFieldHorizontalPadding
private val InboxMenuContentGap = 28.dp
private val InboxListBottomPadding = 112.dp
private val InboxHeaderHeight = 156.dp
private val InboxQuickActionCardHeight = 96.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    padding: PaddingValues,
    sections: List<NotificationSectionData>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRefresh: () -> Unit = {},
    onMarkAllRead: () -> Unit = {},
    onNotificationOpened: (Int?) -> Unit = {},
    onBookingSelected: (BookingInfo) -> Unit,
    onMessageSelected: (ConversationInfo) -> Unit,
    onNotificationSelected: (NotificationItem) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    var selectedCategory by rememberSaveable { mutableStateOf<InboxCategoryType?>(null) }
    val filteredSections = sections.mapNotNull { section ->
        val filteredItems = section.items.filter { item ->
            selectedCategory == null || item.category == selectedCategory
        }
        if (filteredItems.isEmpty()) null else section.copy(items = filteredItems)
    }
    val quickActions = inboxQuickActions(sections)
    val hasVisibleContent = filteredSections.any { it.items.isNotEmpty() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = padding.calculateStartPadding(layoutDirection),
                end = padding.calculateEndPadding(layoutDirection),
                bottom = padding.calculateBottomPadding()
            ),
        contentPadding = PaddingValues(bottom = InboxListBottomPadding)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(InboxHeaderHeight + (InboxQuickActionCardHeight / 2))
            ) {
                InboxHeader(modifier = Modifier.align(Alignment.TopStart))
                InboxQuickActions(
                    categories = quickActions,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = if (selectedCategory == category) null else category
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp)
                        .offset(y = InboxHeaderHeight - (InboxQuickActionCardHeight / 2))
                )
            }
        }

        if (isLoading && !hasVisibleContent) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppScreenHorizontalPadding, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AppRotatingLoadingIndicator(
                        label = null,
                        iconSize = 28.dp
                    )
                }
            }
            item {
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        }
        errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppScreenHorizontalPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                    Text(
                        text = "Thử lại",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onRefresh)
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(InboxMenuContentGap))
        }

        filteredSections.forEach { section ->
            item {
                NotificationSection(
                    section = section,
                    onMarkAllClick = onMarkAllRead,
                    onItemClick = { item ->
                        when {
                            item.category == InboxCategoryType.Booking && item.bookingInfo != null -> {
                                onNotificationOpened(item.bookingInfo.notificationId ?: item.id)
                                onBookingSelected(item.bookingInfo)
                            }
                            item.category == InboxCategoryType.Message && item.conversationInfo != null -> {
                                onMessageSelected(item.conversationInfo)
                            }
                            item.detailInfo != null -> {
                                onNotificationOpened(item.id)
                                onNotificationSelected(item)
                            }
                            else -> Unit
                        }
                    },
                    modifier = Modifier.padding(horizontal = AppScreenHorizontalPadding)
                )
                Spacer(Modifier.height(InboxSectionGap))
            }
        }

        if (!isLoading && errorMessage.isNullOrBlank() && filteredSections.all { it.items.isEmpty() }) {
            item {
                Text(
                    text = "Hộp thư chưa có dữ liệu.",
                    modifier = Modifier.padding(horizontal = AppScreenHorizontalPadding),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

}

@Composable
fun InboxHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(156.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner_app),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentScale = ContentScale.Crop,
            alpha = 1f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.08f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = AppScreenHorizontalPadding, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeaderIconButton(icon = Icons.Outlined.Search)
                    HeaderIconButton(icon = Icons.Outlined.Tune)
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun InboxQuickActions(
    categories: List<InboxCategory>,
    selectedCategory: InboxCategoryType?,
    onCategorySelected: (InboxCategoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppInputCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { item ->
                QuickActionItem(
                    item = item,
                    isSelected = selectedCategory == item.type,
                    onClick = { onCategorySelected(item.type) }
                )
            }
        }
    }
}

@Composable
fun QuickActionItem(
    item: InboxCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconBackground = if (isSelected) {
        AppHeaderGradientEnd.copy(alpha = 0.18f)
    } else {
        item.background
    }
    val iconTint = if (isSelected) AppHeaderGradientStart else item.iconTint
    val borderColor = if (isSelected) AppHeaderGradientStart.copy(alpha = 0.45f) else Color.Transparent

    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(48.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBackground, CircleShape)
                    .border(1.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (item.badgeCount > 0) {
                NotificationBadge(
                    value = item.badgeCount.toString(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NotificationSection(
    section: NotificationSectionData,
    onMarkAllClick: () -> Unit = {},
    onItemClick: (NotificationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val sharedTitleStyle = responsiveSharedTitleStyle(LocalConfiguration.current.screenWidthDp)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.title,
                style = sharedTitleStyle,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (section.showMarkAll) {
                Text(
                    text = stringResource(R.string.inbox_mark_read),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onMarkAllClick)
                )
            }
        }
        Spacer(Modifier.height(InboxSectionGap))
        section.items.forEachIndexed { index, item ->
            NotificationCard(item = item, onClick = { onItemClick(item) })
            if (index != section.items.lastIndex) {
                Spacer(Modifier.height(InboxSectionGap))
            }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationItem, onClick: () -> Unit) {
    val sharedTitleStyle = responsiveSharedTitleStyle(LocalConfiguration.current.screenWidthDp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(item.iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                val subtitleLooksSecondary = looksLikeBookingCodePreview(item.subtitle)
                val isMessageItem = item.category == InboxCategoryType.Message
                val subtitleStyle =
                    if (subtitleLooksSecondary) MaterialTheme.typography.bodySmall
                    else MaterialTheme.typography.bodyMedium
                val subtitleWeight =
                    if (subtitleLooksSecondary || isMessageItem) FontWeight.Normal else FontWeight.Medium
                val subtitleColor =
                    if (subtitleLooksSecondary) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.title,
                        style = sharedTitleStyle,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        softWrap = true,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        text = item.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.subtitle,
                            style = subtitleStyle,
                            fontWeight = subtitleWeight,
                            color = subtitleColor
                        )
                        if (item.detail.isNotBlank()) {
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = item.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    NotificationStatusIndicator(
                        item = item,
                        modifier = Modifier.width(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationStatusIndicator(
    item: NotificationItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        when {
            item.badgeCount > 0 -> {
                NotificationBadge(value = item.badgeCount.toString())
            }

            item.unread -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun NotificationBadge(value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(MaterialTheme.colorScheme.error, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError,
            fontSize = 10.sp
        )
    }
}

private fun looksLikeBookingCodePreview(text: String): Boolean {
    val normalized = text.trim()
    return normalized.contains("#B", ignoreCase = true) ||
        normalized.contains("Mã đặt sân", ignoreCase = true) ||
        normalized.contains("Booking #B", ignoreCase = true)
}

@Composable
private fun NotificationDetailSheet(item: NotificationItem, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val sharedTitleStyle = responsiveSharedTitleStyle(LocalConfiguration.current.screenWidthDp)

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.title,
                        style = sharedTitleStyle,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        softWrap = true,
                        overflow = TextOverflow.Clip
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${item.timeLabel} • ${item.subtitle}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (item.category == InboxCategoryType.Booking && item.bookingInfo != null) {
            BookingHighlightCard(info = item.bookingInfo)
            Spacer(Modifier.height(14.dp))
            VenueInfoCard(info = item.bookingInfo)
            Spacer(Modifier.height(14.dp))
            BookingDetailCard(info = item.bookingInfo)
            Spacer(Modifier.height(12.dp))
            BookerInfoCard(info = item.bookingInfo)
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppCtaWideHeight),
                shape = RoundedCornerShape(AppCtaCornerRadius),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "LIÊN HỆ CHỦ SÂN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else {
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (item.detail.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.longDetail.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = item.longDetail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun BookingDetailCard(info: BookingInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(
                title = "Thông tin lịch đặt",
                icon = Icons.Outlined.EventAvailable
            )
            Spacer(Modifier.height(10.dp))
            BookingDetailRow(label = "Khung giờ", value = info.timeRange)
            BookingDetailRow(label = "Ngày", value = info.dateLabel)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Mã booking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.42f)
                )
                Row(
                    modifier = Modifier.weight(0.58f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = info.bookingCode,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Trạng thái",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.42f)
                )
                Row(
                    modifier = Modifier.weight(0.58f),
                    horizontalArrangement = Arrangement.End
                ) {
                    StatusBadge(label = info.statusLabel)
                }
            }
            BookingDetailRow(label = "Phương thức thanh toán", value = info.paymentMethod)
            BookingDetailRow(label = "Tổng tiền", value = info.totalAmount, highlight = true)
        }
    }
}

@Composable
private fun BookingDetailRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = if (highlight) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun StatusBadge(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(AppPillCornerRadius)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BookingHighlightCard(info: BookingInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = "Đơn đặt sân của bạn đã được xác nhận",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sân sẽ sẵn sàng đón bạn theo lịch hẹn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VenueInfoCard(info: BookingInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.field_football),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 96.dp, height = 72.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(AppCardCornerRadius)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.fieldName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = info.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BookerInfoCard(info: BookingInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(
                title = "Thông tin người đặt",
                icon = Icons.Outlined.Person
            )
            Spacer(Modifier.height(10.dp))
            BookingDetailRow(label = "Tên", value = info.customerName)
            BookingDetailRow(label = "Số điện thoại", value = info.customerPhone)
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Immutable
data class NotificationItem(
    val id: Int? = null,
    val type: String? = null,
    val title: String,
    val subtitle: String,
    val detail: String,
    val longDetail: String = "",
    val timeLabel: String,
    val unread: Boolean = false,
    val badgeCount: Int = 0,
    val bookingId: Int? = null,
    val fieldId: Int? = null,
    val conversationId: Int? = null,
    val category: InboxCategoryType,
    val bookingInfo: BookingInfo? = null,
    val conversationInfo: ConversationInfo? = null,
    val detailInfo: NotificationDetailInfo? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBackground: Color,
    val iconTint: Color
)

@Immutable
data class BookingInfo(
    val fieldName: String,
    val timeRange: String,
    val dateLabel: String,
    val bookingCode: String,
    val statusLabel: String,
    val statusCode: String = "paid",
    val address: String,
    val paymentMethod: String,
    val totalAmount: String,
    val transactionId: String = "",
    val orderId: String = "",
    val checkInCode: String = "",
    val shareUrl: String = "",
    val customerName: String,
    val customerPhone: String,
    val ownerPhone: String = "",
    val ownerNote: String,
    val fieldId: Int? = null,
    val bookingId: Int? = null,
    val notificationId: Int? = null
)

@Immutable
data class ConversationInfo(
    val fieldName: String,
    val statusLabel: String,
    val phoneNumber: String,
    val avatarRes: Int,
    val conversationId: Int? = null,
    val fieldId: Int? = null,
    val bookingId: Int? = null
)

sealed class NotificationDetailInfo {
    data class UpcomingMatch(
        val title: String,
        val subtitle: String,
        val notificationId: Int? = null,
        val fieldId: Int? = null,
        val bookingId: Int? = null,
        val fieldName: String,
        val address: String,
        val timeRange: String,
        val dateLabel: String,
        val bookingCode: String,
        val statusLabel: String,
        val paymentMethod: String,
        val totalAmount: String,
        val reminderText: String,
        val phoneNumber: String,
        val avatarRes: Int
    ) : NotificationDetailInfo()

    data class Promotion(
        val title: String,
        val subtitle: String,
        val notificationId: Int? = null,
        val promoTitle: String,
        val promoSubtitle: String,
        val contentText: String,
        val periodText: String,
        val conditions: List<String>
    ) : NotificationDetailInfo()

    data class SystemNotice(
        val title: String,
        val subtitle: String,
        val notificationId: Int? = null,
        val contentText: String,
        val features: List<String>,
        val timeText: String
    ) : NotificationDetailInfo()
}

@Immutable
data class InboxCategory(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int,
    val type: InboxCategoryType,
    val background: Color,
    val iconTint: Color
)

enum class InboxCategoryType {
    Booking,
    Activity,
    Message,
    Support
}

@Immutable
data class NotificationSectionData(
    val title: String,
    val showMarkAll: Boolean,
    val items: List<NotificationItem>
)

@Composable
private fun inboxQuickActions(sections: List<NotificationSectionData>): List<InboxCategory> {
    val badgeCounts = InboxCategoryType.values().associateWith { category ->
        sections
            .flatMap { it.items }
            .filter { it.category == category && it.unread }
            .sumOf { item -> item.badgeCount.takeIf { it > 0 } ?: 1 }
    }

    return listOf(
        InboxCategory(
            label = "Đặt sân",
            icon = Icons.Outlined.EventAvailable,
            badgeCount = badgeCounts[InboxCategoryType.Booking] ?: 0,
            type = InboxCategoryType.Booking,
            background = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            iconTint = MaterialTheme.colorScheme.primary
        ),
        InboxCategory(
            label = "Hoạt động",
            icon = Icons.Outlined.Notifications,
            badgeCount = badgeCounts[InboxCategoryType.Activity] ?: 0,
            type = InboxCategoryType.Activity,
            background = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
            iconTint = MaterialTheme.colorScheme.onSecondary
        ),
        InboxCategory(
            label = "Tin nhắn",
            icon = Icons.Outlined.ChatBubble,
            badgeCount = badgeCounts[InboxCategoryType.Message] ?: 0,
            type = InboxCategoryType.Message,
            background = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
            iconTint = MaterialTheme.colorScheme.tertiary
        ),
        InboxCategory(
            label = "Hỗ trợ",
            icon = Icons.Outlined.HelpOutline,
            badgeCount = badgeCounts[InboxCategoryType.Support] ?: 0,
            type = InboxCategoryType.Support,
            background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun inboxSections(): List<NotificationSectionData> {
    val accentPrimary = MaterialTheme.colorScheme.primary
    val accentSecondary = MaterialTheme.colorScheme.secondary
    val accentTertiary = MaterialTheme.colorScheme.tertiary
    val infoTint = MaterialTheme.colorScheme.onSurface

    val priorityItems = listOf(
        NotificationItem(
            title = "Đặt sân thành công",
            subtitle = "Sân Mỹ Đình Mini • 18:00 hôm nay",
            detail = "Booking #B123456 đã được xác nhận.",
            longDetail = "Bạn có thể đến sân trước 10 phút để hoàn tất thủ tục nhận sân.",
            timeLabel = "10:30",
            unread = true,
            category = InboxCategoryType.Booking,
            bookingInfo = BookingInfo(
                fieldName = "Sân Mỹ Đình Mini",
                timeRange = "18:00 - 19:30",
                dateLabel = "Hôm nay, 23/05/2026",
                bookingCode = "#B123456",
                statusLabel = "Đã xác nhận",
                address = "Đường Lê Đức Thọ, Mỹ Đình, Nam Từ Liêm, Hà Nội",
                paymentMethod = "Ví điện tử",
                totalAmount = "150.000 đ",
                customerName = "Nguyễn Văn An",
                customerPhone = "090 789 0123",
                ownerPhone = "090 123 4567",
                ownerNote = "Bạn vui lòng đến trước 10 phút để check sân nhé!"
            ),
            icon = Icons.Outlined.EventAvailable,
            iconBackground = accentPrimary.copy(alpha = 0.12f),
            iconTint = accentPrimary
        ),
        NotificationItem(
            title = "Sắp đến giờ thi đấu",
            subtitle = "Còn 1 giờ nữa tới lịch đặt sân",
            detail = "Sân Hoàng Mai • 17:00 hôm nay",
            longDetail = "Bạn vui lòng có mặt đúng giờ để tránh mất lượt đặt sân.",
            timeLabel = "15:00",
            unread = true,
            category = InboxCategoryType.Activity,
            detailInfo = NotificationDetailInfo.UpcomingMatch(
                title = "Sắp đến giờ thi đấu",
                subtitle = "Bạn có lịch đặt sân lúc 17:00 hôm nay.",
                fieldName = "Sân Hoàng Mai",
                address = "Đường Hoàng Mai, Hoàng Mai, Hà Nội",
                timeRange = "17:00 - 18:30",
                dateLabel = "Hôm nay, 23/05/2026",
                bookingCode = "#B987654",
                statusLabel = "Đã xác nhận",
                paymentMethod = "Tiền mặt",
                totalAmount = "200.000 đ",
                reminderText = "Còn 1 giờ nữa tới lịch đặt sân. Đừng quên đến trước 10 phút để có trải nghiệm tốt nhất nhé!",
                phoneNumber = "090 789 0123",
                avatarRes = R.drawable.field_football
            ),
            icon = Icons.Outlined.Notifications,
            iconBackground = accentSecondary.copy(alpha = 0.2f),
            iconTint = MaterialTheme.colorScheme.onSecondary
        ),
        NotificationItem(
            title = "Ưu đãi dành cho bạn",
            subtitle = "Giảm 20% cho khung giờ sáng",
            detail = "Áp dụng đến 30/05/2026",
            longDetail = "Ưu đãi áp dụng cho các khung giờ trước 10:00 và không cộng dồn với chương trình khác.",
            timeLabel = "09:00",
            unread = true,
            category = InboxCategoryType.Activity,
            detailInfo = NotificationDetailInfo.Promotion(
                title = "Ưu đãi dành cho bạn",
                subtitle = "Giảm 20% cho khung giờ sáng",
                promoTitle = "GIẢM 20%",
                promoSubtitle = "KHUNG GIỜ SÁNG",
                contentText = "Giảm 20% cho tất cả các khung giờ từ 6:00 - 11:00. Áp dụng cho tất cả sân trên hệ thống.",
                periodText = "Từ 20/05/2026 đến 30/05/2026",
                conditions = listOf(
                    "Áp dụng cho đặt sân qua ứng dụng",
                    "Không áp dụng với các chương trình ưu đãi khác"
                )
            ),
            icon = Icons.Outlined.LocalOffer,
            iconBackground = accentTertiary.copy(alpha = 0.14f),
            iconTint = accentTertiary
        )
    )

    val messageItems = listOf(
        NotificationItem(
            title = "Sân Mỹ Đình đã phản hồi",
            subtitle = "Sân Mỹ Đình Mini đã gửi cho bạn một tin nhắn mới.",
            detail = "",
            longDetail = "Chủ sân đã xác nhận sân vẫn trống. Bạn có muốn đặt thêm khung giờ tối không?",
            timeLabel = "Hôm qua",
            badgeCount = 2,
            category = InboxCategoryType.Message,
            conversationInfo = ConversationInfo(
                fieldName = "Sân Mỹ Đình Mini",
                statusLabel = "Đang hoạt động",
                phoneNumber = "090 789 0123",
                avatarRes = R.drawable.field_football
            ),
            icon = Icons.Outlined.ChatBubble,
            iconBackground = accentPrimary.copy(alpha = 0.12f),
            iconTint = accentPrimary
        ),
        NotificationItem(
            title = "FC Phoenix muốn giao lưu",
            subtitle = "Đội bóng FC Phoenix muốn giao lưu vào cuối tuần này.",
            detail = "",
            longDetail = "Họ muốn giao lưu vào chiều thứ 7. Bạn có thể phản hồi để chốt lịch và sân.",
            timeLabel = "2 ngày trước",
            badgeCount = 1,
            category = InboxCategoryType.Message,
            conversationInfo = ConversationInfo(
                fieldName = "Sân Mỹ Đình Mini",
                statusLabel = "Đang hoạt động",
                phoneNumber = "090 789 0123",
                avatarRes = R.drawable.field_football
            ),
            icon = Icons.Outlined.Notifications,
            iconBackground = accentSecondary.copy(alpha = 0.2f),
            iconTint = MaterialTheme.colorScheme.onSecondary
        )
    )

    val activityItems = listOf(
        NotificationItem(
            title = "Thông báo hệ thống",
            subtitle = "Bảo trì hệ thống vào 02:00 AM ngày 25/05/2026.",
            detail = "",
            longDetail = "Trong thời gian bảo trì, một số tính năng đặt sân có thể tạm thời gián đoạn.",
            timeLabel = "18/05/2026",
            category = InboxCategoryType.Support,
            detailInfo = NotificationDetailInfo.SystemNotice(
                title = "Thông báo hệ thống",
                subtitle = "Cập nhật tính năng mới",
                contentText = "Chúng tôi vừa cập nhật thêm tính năng mới giúp bạn dễ dàng quản lý lịch đặt sân và theo dõi trận đấu.",
                features = listOf(
                    "Quản lý lịch đặt sân tiện lợi hơn",
                    "Nhắc lịch trước trận đấu",
                    "Chat trực tiếp với chủ sân"
                ),
                timeText = "23/05/2026 • 09:00"
            ),
            icon = Icons.Outlined.Notifications,
            iconBackground = infoTint.copy(alpha = 0.08f),
            iconTint = infoTint
        )
    )

    return listOf(
        NotificationSectionData(
            title = stringResource(R.string.inbox_section_priority),
            showMarkAll = true,
            items = priorityItems
        ),
        NotificationSectionData(
            title = stringResource(R.string.inbox_section_messages),
            showMarkAll = false,
            items = messageItems
        ),
        NotificationSectionData(
            title = stringResource(R.string.inbox_section_activity),
            showMarkAll = false,
            items = activityItems
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    info: BookingInfo,
    onBackClick: () -> Unit,
    onOpenChat: (ConversationInfo) -> Unit
) {
    val context = LocalContext.current
    var showContactSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenBackground = MaterialTheme.colorScheme.background
    val qrBitmap = rememberQrBitmap(info.shareUrl)
    val shareText = remember(info) {
        buildString {
            append("Hóa đơn & QR Check-in")
            append('\n')
            append("Sân: ${info.fieldName}")
            append('\n')
            append("Ngày: ${info.dateLabel}")
            append('\n')
            append("Khung giờ: ${info.timeRange}")
            append('\n')
            append("Người đặt: ${info.customerName.ifBlank { "Chưa cập nhật" }}")
            append('\n')
            append("Trạng thái: ${info.statusLabel}")
            if (info.paymentMethod.isNotBlank() || info.totalAmount.isNotBlank()) {
                append('\n')
                append(
                    listOf(info.paymentMethod, info.totalAmount)
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                        .let { "Thanh toán: $it" }
                )
            }
            if (info.shareUrl.isNotBlank()) {
                append('\n')
                append('\n')
                append(info.shareUrl)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            BookingDetailTopBar(
                onBackClick = onBackClick,
                onShareClick = {
                    try {
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                },
                                null
                            )
                        )
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            "Không thể chia sẻ hóa đơn lúc này",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        BookingDetailContent(
            info = info,
            onShareQr = {
                if (qrBitmap == null || info.shareUrl.isBlank()) {
                    Toast.makeText(
                        context,
                        "QR check-in chưa sẵn sàng",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    runCatching { shareQrBitmap(context, qrBitmap, info.bookingCode) }
                        .onFailure {
                            Toast.makeText(
                                context,
                                "Không thể chia sẻ QR lúc này",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            },
            onDownloadQr = {
                if (qrBitmap == null || info.shareUrl.isBlank()) {
                    Toast.makeText(
                        context,
                        "QR check-in chưa sẵn sàng",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    runCatching { saveQrBitmap(context, qrBitmap, info.bookingCode) }
                        .onSuccess {
                            Toast.makeText(
                                context,
                                "Đã tải QR xuống máy",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .onFailure {
                            Toast.makeText(
                                context,
                                "Không thể tải QR xuống",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            },
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppScreenHorizontalPadding, vertical = 12.dp)
            )
        }

        BookingDetailBottomActions(
            onDirectionsClick = {
                val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(info.address)}")
                val intent = Intent(Intent.ACTION_VIEW, geoUri)
                context.startActivity(intent)
            },
            onContactClick = { showContactSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showContactSheet) {
            val ownerPhone = info.ownerPhone.trim()
            val ownerPhoneLabel = ownerPhone.ifBlank { "Chưa có số liên hệ" }
            ModalBottomSheet(
                onDismissRequest = { showContactSheet = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                shape = RoundedCornerShape(topStart = AppSheetTopCornerRadius, topEnd = AppSheetTopCornerRadius)
            ) {
                ContactOwnerSheet(
                    phoneNumber = ownerPhoneLabel,
                    onCloseClick = { showContactSheet = false },
                    onCallClick = {
                        if (ownerPhone.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:${ownerPhone}")
                            context.startActivity(intent)
                        }
                        showContactSheet = false
                    },
                    onMessageClick = {
                        showContactSheet = false
                        onOpenChat(
                            ConversationInfo(
                                fieldName = info.fieldName,
                                statusLabel = "Đang hoạt động",
                                phoneNumber = ownerPhone,
                                avatarRes = R.drawable.field_football,
                                fieldId = info.fieldId,
                                bookingId = info.bookingId
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BookingDetailTopBar(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = AppScreenHorizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = stringResource(R.string.inbox_booking_detail_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onShareClick) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BookingDetailContent(
    info: BookingInfo,
    onShareQr: () -> Unit,
    onDownloadQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BookingStatusCard(info)
        Spacer(Modifier.height(12.dp))
        VenueInfoCard(info)
        Spacer(Modifier.height(12.dp))
        BookingDetailCard(info)
        Spacer(Modifier.height(12.dp))
        BookerInfoCard(info)
        Spacer(Modifier.height(12.dp))
        BookingOwnerNoteCard(note = info.ownerNote)
        Spacer(Modifier.height(16.dp))
        BookingCheckInCard(
            info = info,
            onShareQr = onShareQr,
            onDownloadQr = onDownloadQr
        )
        Spacer(Modifier.height(140.dp))
    }
}

@Composable
private fun BookingStatusCard(info: BookingInfo) {
    val isActive = info.statusCode == "paid" || info.statusCode == "checked_in"
    val cardColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val textColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val title = when (info.statusCode) {
        "checked_in" -> "Đã check-in"
        "expired" -> "Mã check-in đã hết hạn"
        "cancelled" -> "Đơn đặt sân đã hủy"
        else -> "Đặt sân thành công"
    }
    val subtitle = when (info.statusCode) {
        "checked_in" -> "Booking ${info.bookingCode} đã được chủ sân xác nhận check-in."
        "expired" -> "Booking ${info.bookingCode} đã qua thời gian sử dụng."
        "cancelled" -> "Booking ${info.bookingCode} không còn hiệu lực."
        else -> "Booking ${info.bookingCode} đã được thanh toán."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(textColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BookingCheckInCard(
    info: BookingInfo,
    onShareQr: () -> Unit,
    onDownloadQr: () -> Unit
) {
    val qrBitmap = rememberQrBitmap(info.shareUrl)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(
                title = "QR Check-in",
                icon = Icons.Outlined.EventAvailable
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            RoundedCornerShape(AppCardCornerRadius)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR check-in",
                            modifier = Modifier.size(196.dp)
                        )
                    } else {
                        Text(
                            text = "Đang tạo QR...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (info.shareUrl.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onShareQr,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppCtaCornerRadius),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Chia sẻ",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = onDownloadQr,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppCtaCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Tải xuống",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberQrBitmap(
    content: String,
    size: Int = 768
): Bitmap? = remember(content, size) {
    if (content.isBlank()) {
        null
    } else {
        runCatching {
            val matrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                mapOf(EncodeHintType.MARGIN to 1)
            )
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        setPixel(
                            x,
                            y,
                            if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE,
                        )
                    }
                }
            }
        }.getOrNull()
    }
}

private fun shareQrBitmap(
    context: android.content.Context,
    bitmap: Bitmap,
    bookingCode: String
) {
    val fileName = "qr_${bookingCode.trim('#', 'B', 'b').ifBlank { "checkin" }}.png"
    val cacheDir = File(context.cacheDir, "shared_qr").apply { mkdirs() }
    val file = File(cacheDir, fileName)

    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = android.content.ClipData.newRawUri("QR Check-in", uri)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ QR Check-in"))
}

private fun saveQrBitmap(
    context: android.content.Context,
    bitmap: Bitmap,
    bookingCode: String
) {
    val fileName = "qr_${bookingCode.trim('#', 'B', 'b').ifBlank { "checkin" }}_${System.currentTimeMillis()}.png"
    val resolver = context.contentResolver

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = android.content.ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_PICTURES}/SportManagement")
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create media entry")
        resolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } ?: error("Unable to open media output stream")
    } else {
        val picturesDir = android.os.Environment
            .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
        val targetDir = File(picturesDir, "SportManagement").apply { mkdirs() }
        val file = File(targetDir, fileName)
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
    }
}

@Composable
private fun BookingOwnerNoteCard(note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(
                title = "Ghi chú từ chủ sân",
                icon = Icons.Outlined.Notifications
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BookingDetailBottomActions(
    onDirectionsClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = AppCardCornerRadius,
            topEnd = AppCardCornerRadius
        ),
        color = Color.White,
        tonalElevation = 4.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppScreenHorizontalPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDirectionsClick,
                modifier = Modifier
                    .weight(1f)
                    .height(AppCtaWideHeight),
                shape = RoundedCornerShape(AppCtaCornerRadius),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.inbox_booking_directions),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(
                onClick = onContactClick,
                modifier = Modifier
                    .weight(1f)
                    .height(AppCtaWideHeight),
                shape = RoundedCornerShape(AppCtaCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.inbox_booking_contact_owner),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ContactOwnerSheet(
    phoneNumber: String,
    onCloseClick: () -> Unit,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppScreenHorizontalPadding, vertical = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.inbox_contact_owner_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        ContactOwnerOption(
            title = stringResource(R.string.inbox_contact_owner_call),
            subtitle = phoneNumber,
            icon = Icons.Outlined.Phone,
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = onCallClick
        )
        Spacer(Modifier.height(10.dp))
        ContactOwnerOption(
            title = stringResource(R.string.inbox_contact_owner_message),
            subtitle = stringResource(R.string.inbox_contact_owner_message_hint),
            icon = Icons.Outlined.ChatBubble,
            iconTint = MaterialTheme.colorScheme.tertiary,
            onClick = onMessageClick
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ContactOwnerOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    info: NotificationDetailInfo,
    onBackClick: () -> Unit,
    onOpenChat: (ConversationInfo) -> Unit,
    onPromotionAction: () -> Unit
) {
    val context = LocalContext.current
    var showContactSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenBackground = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NotificationDetailTopBar(onBackClick = onBackClick)
            NotificationDetailContent(
                info = info,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppScreenHorizontalPadding, vertical = 12.dp)
            )
        }

        when (info) {
            is NotificationDetailInfo.UpcomingMatch -> {
                NotificationDetailBottomAction(
                    label = stringResource(R.string.inbox_booking_contact_owner),
                    onClick = { showContactSheet = true },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            is NotificationDetailInfo.Promotion -> {
                NotificationDetailBottomAction(
                    label = stringResource(R.string.inbox_promo_action),
                    onClick = onPromotionAction,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            is NotificationDetailInfo.SystemNotice -> Unit
        }

        if (showContactSheet && info is NotificationDetailInfo.UpcomingMatch) {
            ModalBottomSheet(
                onDismissRequest = { showContactSheet = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                shape = RoundedCornerShape(topStart = AppSheetTopCornerRadius, topEnd = AppSheetTopCornerRadius)
            ) {
                ContactOwnerSheet(
                    phoneNumber = info.phoneNumber,
                    onCloseClick = { showContactSheet = false },
                    onCallClick = {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:${info.phoneNumber}")
                        context.startActivity(intent)
                        showContactSheet = false
                    },
                    onMessageClick = {
                        showContactSheet = false
                        onOpenChat(
                            ConversationInfo(
                                fieldName = info.fieldName,
                                statusLabel = "Đang hoạt động",
                                phoneNumber = info.phoneNumber,
                                avatarRes = info.avatarRes,
                                fieldId = info.fieldId,
                                bookingId = info.bookingId
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationDetailTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = AppScreenHorizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = stringResource(R.string.inbox_notification_detail_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationDetailContent(
    info: NotificationDetailInfo,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        when (info) {
            is NotificationDetailInfo.UpcomingMatch -> {
                NotificationBannerCard(
                    title = info.title,
                    subtitle = info.subtitle,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    icon = Icons.Outlined.Notifications,
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.height(12.dp))
                UpcomingMatchVenueCard(info)
                Spacer(Modifier.height(12.dp))
                UpcomingMatchBookingCard(info)
                Spacer(Modifier.height(12.dp))
                ReminderCard(info.reminderText)
                Spacer(Modifier.height(72.dp))
            }
            is NotificationDetailInfo.Promotion -> {
                NotificationBannerCard(
                    title = info.title,
                    subtitle = info.subtitle,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    icon = Icons.Outlined.LocalOffer,
                    iconTint = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(12.dp))
                PromotionHeroCard(info)
                Spacer(Modifier.height(12.dp))
                PromotionContentCard(info)
                Spacer(Modifier.height(72.dp))
            }
            is NotificationDetailInfo.SystemNotice -> {
                NotificationBannerCard(
                    title = info.title,
                    subtitle = info.subtitle,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    icon = Icons.Outlined.Info,
                    iconTint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                SystemIllustrationCard()
                Spacer(Modifier.height(12.dp))
                SystemNoticeContentCard(info)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NotificationBannerCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UpcomingMatchVenueCard(info: NotificationDetailInfo.UpcomingMatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = info.avatarRes),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 90.dp, height = 64.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(AppCardCornerRadius)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.fieldName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = info.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingMatchBookingCard(info: NotificationDetailInfo.UpcomingMatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(
                title = "Thông tin lịch đặt",
                icon = Icons.Outlined.Schedule
            )
            Spacer(Modifier.height(10.dp))
            BookingDetailRow(label = "Khung giờ", value = info.timeRange)
            BookingDetailRow(label = "Ngày đặt", value = info.dateLabel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã booking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = info.bookingCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trạng thái",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusBadge(label = info.statusLabel)
            }
            Spacer(Modifier.height(6.dp))
            BookingDetailRow(label = "Phương thức thanh toán", value = info.paymentMethod)
            BookingDetailRow(label = "Tổng tiền", value = info.totalAmount, highlight = true)
        }
    }
}

@Composable
private fun ReminderCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(title = "Nhắc bạn", icon = Icons.Outlined.Notifications)
            Spacer(Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PromotionHeroCard(info: NotificationDetailInfo.Promotion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = info.promoTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = info.promoSubtitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PromotionContentCard(info: NotificationDetailInfo.Promotion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(title = "Nội dung ưu đãi", icon = Icons.Outlined.LocalOffer)
            Spacer(Modifier.height(8.dp))
            Text(
                text = info.contentText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            SectionHeader(title = "Thời gian áp dụng", icon = Icons.Outlined.Schedule)
            Spacer(Modifier.height(8.dp))
            Text(
                text = info.periodText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            SectionHeader(title = "Điều kiện áp dụng", icon = Icons.Outlined.Notifications)
            Spacer(Modifier.height(8.dp))
            info.conditions.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun SystemIllustrationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    ),
                    RoundedCornerShape(AppCardCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun SystemNoticeContentCard(info: NotificationDetailInfo.SystemNotice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(title = "Nội dung", icon = Icons.Outlined.Info)
            Spacer(Modifier.height(8.dp))
            Text(
                text = info.contentText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            SectionHeader(title = "Tính năng mới", icon = Icons.Outlined.Notifications)
            Spacer(Modifier.height(8.dp))
            info.features.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(12.dp))
            SectionHeader(title = "Thời gian", icon = Icons.Outlined.Schedule)
            Spacer(Modifier.height(8.dp))
            Text(
                text = info.timeText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationDetailBottomAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 12.dp
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppScreenHorizontalPadding, vertical = 12.dp)
                .height(AppCtaWideHeight),
            shape = RoundedCornerShape(AppCtaCornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ConversationScreen(
    info: ConversationInfo,
    messages: List<ConversationMessageUi>,
    draft: String,
    isSending: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val screenBackground = MaterialTheme.colorScheme.background
    val listState = rememberLazyListState()
    val sortedMessages = remember(messages) { messages.sortedBy { it.id } }
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val bottomAnchorIndex =
        1 + sortedMessages.size +
            (if (isLoading) 1 else 0) +
            (if (!errorMessage.isNullOrBlank()) 1 else 0)

    LaunchedEffect(bottomAnchorIndex, imeBottom) {
        listState.animateScrollToItem(bottomAnchorIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .imePadding()
    ) {
        ConversationTopBar(
            info = info,
            onBackClick = onBackClick,
            onCallClick = {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${info.phoneNumber}")
                context.startActivity(intent)
            }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = AppScreenHorizontalPadding),
            contentPadding = PaddingValues(bottom = 8.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            item {
                Text(
                    text = "Hôm nay",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (isLoading) {
                item {
                    Text(
                        text = "Đang tải tin nhắn...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = message, color = MaterialTheme.colorScheme.error)
                        Text(text = "Thử lại", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onRetry))
                    }
                }
            }

            items(sortedMessages.size, key = { index -> sortedMessages[index].id }) { index ->
                ConversationMessageBubble(sortedMessages[index])
            }

            item(key = "conversation-bottom-anchor") {
                Spacer(Modifier.height(1.dp))
            }
        }

        ConversationInputBar(
            draft = draft,
            isSending = isSending,
            onDraftChange = onDraftChange,
            onSend = onSend,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun ConversationTopBar(
    info: ConversationInfo,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = AppScreenHorizontalPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(modifier = Modifier.size(42.dp)) {
            Image(
                painter = painterResource(id = info.avatarRes),
                contentDescription = null,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = info.fieldName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = info.statusLabel,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onCallClick) {
            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class ConversationMessageUi(
    val id: Int,
    val text: String,
    val time: String,
    val isUser: Boolean
)

@Composable
private fun ConversationMessageBubble(message: ConversationMessageUi) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Column(horizontalAlignment = alignment, modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(AppCardCornerRadius),
            modifier = Modifier.widthIn(max = screenWidth * 0.76f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = message.time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConversationInputBar(
    draft: String,
    isSending: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppScreenHorizontalPadding, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            TextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp),
                placeholder = { Text(stringResource(R.string.inbox_chat_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(AppPillCornerRadius)
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(enabled = !isSending, onClick = onSend),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
