package com.sportmanagement.manager.ui.screens.bookings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.manager.domain.model.BookingHistoryEvent
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.domain.model.BookingStatus
import com.sportmanagement.manager.domain.model.historyEvents
import java.text.NumberFormat
import java.util.Locale

// ── Status colors ─────────────────────────────────────────────────────────────

private val BookingStatus.color: Color
    get() = when (this) {
        BookingStatus.PENDING   -> Color(0xFFF59E0B)
        BookingStatus.CONFIRMED -> Color(0xFF16A34A)
        BookingStatus.COMPLETED -> Color(0xFF64748B)
        BookingStatus.CANCELLED -> Color(0xFFDC2626)
    }

private val BookingStatus.bgColor: Color
    get() = when (this) {
        BookingStatus.PENDING   -> Color(0xFFFEF3C7)
        BookingStatus.CONFIRMED -> Color(0xFFDCFCE7)
        BookingStatus.COMPLETED -> Color(0xFFF1F5F9)
        BookingStatus.CANCELLED -> Color(0xFFFFE4E6)
    }

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: BookingItem,
    isStartingChat: Boolean = false,
    onBackClick: () -> Unit,
    onConfirm: (String) -> Unit = {},
    onCancel: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onPaymentConfirm: (String) -> Unit = {},
    onMessageCustomer: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("THÔNG TIN", "KHÁCH HÀNG", "LỊCH SỬ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // ── Top App Bar ───────────────────────────────────────────────────────
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Chi tiết đặt sân",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "#${booking.id.uppercase()}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(booking.status.bgColor)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = booking.status.badgeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = booking.status.color
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            modifier = Modifier.shadow(3.dp)
        )

        // ── Summary card ─────────────────────────────────────────────────────
        BookingSummaryCard(booking)

        // ── Tabs ─────────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.shadow(1.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // ── Tab content ───────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> infoTabContent(booking, onConfirm, onCancel, onEdit, onPaymentConfirm)
                1 -> customerTabContent(booking, isStartingChat, onMessageCustomer)
                2 -> historyTabContent(booking)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Summary card ──────────────────────────────────────────────────────────────

@Composable
private fun BookingSummaryCard(booking: BookingItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp)
            .background(Color.White)
    ) {
        // Colored left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(80.dp)
                .align(Alignment.CenterStart)
                .background(booking.status.color)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Filled.SportsSoccer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = booking.pitchName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${booking.courtCode} · ${booking.courtName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${booking.dayOfWeek}, ${booking.date}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "${booking.startTime} – ${booking.endTime}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${booking.durationMinutes} phút",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = formatVnd(booking.totalPrice) + "đ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = booking.status.color
                )
            }
        }
    }
}

// ── Info tab ──────────────────────────────────────────────────────────────────

private fun LazyListScope.infoTabContent(
    booking: BookingItem,
    onConfirm: (String) -> Unit,
    onCancel: (String) -> Unit,
    onEdit: (String) -> Unit,
    onPaymentConfirm: (String) -> Unit
) {
    item {
        SectionCard {
            SectionHeader("THÔNG TIN ĐẶT SÂN")
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "Cơ sở",    value = booking.pitchName)
            InfoRow(label = "Sân con",  value = "${booking.courtName} (${booking.courtCode})")
            InfoRow(label = "Ngày",     value = "${booking.dayOfWeek}, ${booking.date}")
            InfoRow(label = "Giờ",      value = "${booking.startTime} – ${booking.endTime}")
            InfoRow(label = "Thời lượng", value = "${booking.durationMinutes} phút")
        }
    }
    item {
        SectionCard {
            SectionHeader("THANH TOÁN")
            Spacer(Modifier.height(8.dp))
            PaymentRow(label = "Đơn giá", value = formatVnd(booking.pricePerHour) + "đ/h")
            PaymentRow(label = "Thời gian", value = String.format("%.1fh", booking.durationMinutes / 60.0))
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFEEEEEE))
            PaymentRow(
                label = "Tổng cộng",
                value = formatVnd(booking.totalPrice) + "đ",
                valueBold = true,
                valueColor = MaterialTheme.colorScheme.onBackground
            )
            if (booking.depositPaid > 0) {
                PaymentRow(
                    label = "Đã cọc",
                    value = "– " + formatVnd(booking.depositPaid) + "đ",
                    valueColor = Color(0xFF16A34A)
                )
                val remaining = booking.totalPrice - booking.depositPaid
                if (remaining > 0) {
                    PaymentRow(
                        label = "Còn lại",
                        value = formatVnd(remaining) + "đ",
                        valueColor = Color(0xFFDC2626),
                        valueBold = true
                    )
                }
            }
            if (booking.paymentMethod.isNotBlank()) {
                PaymentRow(label = "Phương thức", value = booking.paymentMethod)
            }
            Spacer(Modifier.height(6.dp))
            // Payment status pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (booking.isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (booking.isPaid) Icons.Filled.CheckCircle else Icons.Filled.Payments,
                    contentDescription = null,
                    tint = if (booking.isPaid) Color(0xFF16A34A) else Color(0xFFF59E0B),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (booking.isPaid) "Đã thanh toán đầy đủ" else "Chưa thanh toán",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (booking.isPaid) Color(0xFF16A34A) else Color(0xFFB45309)
                )
            }
        }
    }
    if (booking.notes.isNotBlank()) {
        item {
            SectionCard {
                SectionHeader("GHI CHÚ")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = booking.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    item { BookingActionsSection(booking, onConfirm, onCancel, onEdit, onPaymentConfirm) }
}

// ── Customer tab ──────────────────────────────────────────────────────────────

private fun LazyListScope.customerTabContent(
    booking: BookingItem,
    isStartingChat: Boolean,
    onMessageCustomer: () -> Unit
) {
    item {
        val context = LocalContext.current
        SectionCard {
            // Avatar + name row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = booking.customer.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = booking.customer.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (booking.customer.isVip) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "VIP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Thành viên từ ${booking.customer.memberSince}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(12.dp))

            // Phone row
            ContactInfoRow(
                icon = Icons.Filled.Call,
                label = "Điện thoại",
                value = booking.customer.phone.ifBlank { "—" }
            )
            Spacer(Modifier.height(4.dp))
            ContactInfoRow(
                icon = Icons.Filled.Chat,
                label = "Email",
                value = booking.customer.email.ifBlank { "—" }
            )

            Spacer(Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!booking.isManagerCreated) {
                    Button(
                        onClick = onMessageCustomer,
                        modifier = Modifier.weight(1f),
                        enabled = !isStartingChat,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isStartingChat) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(if (isStartingChat) "Đang mở..." else "NHẮN TIN", fontSize = 13.sp)
                    }
                }
                OutlinedButton(
                    onClick = {
                        val phone = booking.customer.phone.trim()
                        if (phone.isNotBlank()) {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phone")
                                }
                            )
                        }
                    },
                    modifier = if (booking.isManagerCreated) Modifier.fillMaxWidth() else Modifier.weight(1f),
                    enabled = booking.customer.phone.isNotBlank(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("GỌI ĐIỆN", fontSize = 13.sp)
                }
            }
        }
    }

    // Stats card
    item {
        SectionCard {
            SectionHeader("THỐNG KÊ")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "TỔNG ĐẶT SÂN",
                    value = "${booking.customer.totalBookings} lần"
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(Color(0xFFEEEEEE))
                )
                StatItem(
                    label = "TỔNG CHI TIÊU",
                    value = formatVnd(booking.customer.totalSpend) + "đ"
                )
            }
        }
    }
}

// ── History tab ───────────────────────────────────────────────────────────────

private fun LazyListScope.historyTabContent(booking: BookingItem) {
    val events = booking.historyEvents
    if (events.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có lịch sử", color = MaterialTheme.colorScheme.outline)
            }
        }
        return
    }
    item {
        SectionCard {
            SectionHeader("LỊCH SỬ ĐƠN ĐẶT SÂN")
            Spacer(Modifier.height(12.dp))
            events.forEachIndexed { i, event ->
                HistoryEventRow(event = event, isLast = i == events.lastIndex)
            }
        }
    }
}

// ── Actions section ───────────────────────────────────────────────────────────

@Composable
private fun BookingActionsSection(
    booking: BookingItem,
    onConfirm: (String) -> Unit,
    onCancel: (String) -> Unit,
    onEdit: (String) -> Unit,
    onPaymentConfirm: (String) -> Unit
) {
    when (booking.status) {
        BookingStatus.PENDING -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onConfirm(booking.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("XÁC NHẬN LỊCH ĐẶT", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { onCancel(booking.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.EventBusy, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("TỪ CHỐI", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        BookingStatus.CONFIRMED -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onEdit(booking.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Schedule, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("DỜI LỊCH")
                    }
                    OutlinedButton(
                        onClick = { onCancel(booking.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626))
                    ) {
                        Text("HỦY LỊCH")
                    }
                }
                if (!booking.isPaid) {
                    Button(
                        onClick = { onPaymentConfirm(booking.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Payments, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("XÁC NHẬN THANH TOÁN")
                    }
                }
            }
        }
        BookingStatus.COMPLETED -> {
            if (!booking.isPaid) {
                Button(
                    onClick = { onPaymentConfirm(booking.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Payments, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("XÁC NHẬN THANH TOÁN")
                }
            }
        }
        else -> {}
    }
}

// ── History row ───────────────────────────────────────────────────────────────

@Composable
private fun HistoryEventRow(event: BookingHistoryEvent, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, bottom = if (!isLast) 8.dp else 0.dp)
        ) {
            Text(text = event.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            Text(text = event.action.uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = event.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Text(text = "bởi ${event.author}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// ── Reusable components ───────────────────────────────────────────────────────

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(2f),
            maxLines = 2
        )
    }
}

@Composable
private fun ContactInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(80.dp))
        Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PaymentRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.secondary,
    valueBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = if (valueBold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatVnd(amount: Long): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)
