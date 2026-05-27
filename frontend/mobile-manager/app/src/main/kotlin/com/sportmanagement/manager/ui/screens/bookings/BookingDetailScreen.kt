package com.sportmanagement.manager.ui.screens.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.manager.domain.model.BookingHistoryEvent
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.domain.model.BookingStatus
import com.sportmanagement.manager.domain.model.historyEvents
import java.text.NumberFormat
import java.util.Locale

private val BookingStatus.accent: Color
    get() = when (this) {
        BookingStatus.PENDING -> Color(0xFFF59E0B)
        BookingStatus.CONFIRMED -> Color(0xFF15803D)
        BookingStatus.COMPLETED -> Color(0xFF94A3B8)
        BookingStatus.CANCELLED -> Color(0xFFE11D48)
    }

private val BookingStatus.badgeBg: Color
    get() = when (this) {
        BookingStatus.PENDING -> Color(0xFFFEF3C7)
        BookingStatus.CONFIRMED -> Color(0xFFDCFCE7)
        BookingStatus.COMPLETED -> Color(0xFFE2E8F0)
        BookingStatus.CANCELLED -> Color(0xFFFFE4E6)
    }

private val BookingStatus.badgeText: Color
    get() = when (this) {
        BookingStatus.PENDING -> Color(0xFFB45309)
        BookingStatus.CONFIRMED -> Color(0xFF15803D)
        BookingStatus.COMPLETED -> Color(0xFF64748B)
        BookingStatus.CANCELLED -> Color(0xFFBE123C)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    booking: BookingItem,
    onBackClick: () -> Unit,
    onConfirm: (String) -> Unit = {},
    onCancel: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onPaymentConfirm: (String) -> Unit = {},
    onMessageCustomer: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("CHI TIẾT", "KHÁCH HÀNG", "LỊCH SỬ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Chi tiết đặt sân",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
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
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(booking.status.badgeBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = booking.status.badgeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = booking.status.badgeText
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            modifier = Modifier.shadow(4.dp)
        )

        BookingHeaderCard(booking = booking)

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            modifier = Modifier.shadow(2.dp)
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item { BookingInfoCard(booking = booking) }
                    item { PaymentCard(booking = booking) }
                    if (booking.notes.isNotBlank()) {
                        item { NotesCard(notes = booking.notes) }
                    }
                    item {
                        BookingActions(
                            booking = booking,
                            onConfirm = { onConfirm(booking.id) },
                            onCancel = { onCancel(booking.id) },
                            onEdit = { onEdit(booking.id) },
                            onPaymentConfirm = { onPaymentConfirm(booking.id) }
                        )
                    }
                }
                1 -> {
                    item { CustomerProfileCard(booking = booking, onMessage = onMessageCustomer) }
                    item { CustomerStatsCard(booking = booking) }
                }
                2 -> {
                    items(booking.historyEvents.size) { i ->
                        HistoryEventRow(
                            event = booking.historyEvents[i],
                            isLast = i == booking.historyEvents.lastIndex
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun BookingHeaderCard(booking: BookingItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsSoccer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${booking.pitchName}  •  ${booking.courtName} (${booking.courtCode})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${booking.dayOfWeek}, ${booking.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${booking.startTime} - ${booking.endTime}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${booking.durationMinutes} phút",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun BookingInfoCard(booking: BookingItem) {
    SectionCard(title = "THÔNG TIN SÂN") {
        InfoRow(icon = Icons.Filled.LocationOn, label = "Cơ sở", value = booking.pitchName)
        InfoRow(icon = Icons.Filled.SportsSoccer, label = "Loại sân", value = booking.courtName)
        InfoRow(icon = Icons.Filled.Schedule, label = "Thời gian", value = "${booking.startTime} – ${booking.endTime}")
        InfoRow(icon = Icons.Filled.AccessTime, label = "Thời lượng", value = "${booking.durationMinutes} phút")
    }
}

@Composable
private fun PaymentCard(booking: BookingItem) {
    SectionCard(title = "THANH TOÁN") {
        PaymentRow(label = "Đơn giá", value = formatVnd(booking.pricePerHour) + "đ/h")
        PaymentRow(label = "Thời gian", value = String.format("%.1fh", booking.durationMinutes / 60.0))
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
        PaymentRow(
            label = "Tổng cộng",
            value = formatVnd(booking.totalPrice) + "đ",
            valueColor = MaterialTheme.colorScheme.onBackground,
            bold = true
        )
        if (booking.depositPaid > 0) {
            PaymentRow(
                label = "Đã đặt cọc",
                value = "- " + formatVnd(booking.depositPaid) + "đ",
                valueColor = Color(0xFF15803D)
            )
            val remaining = booking.totalPrice - booking.depositPaid
            if (remaining > 0) {
                PaymentRow(
                    label = "Còn lại",
                    value = formatVnd(remaining) + "đ",
                    valueColor = Color(0xFFE11D48),
                    bold = true
                )
            }
        }
        if (booking.paymentMethod.isNotBlank()) {
            PaymentRow(label = "Phương thức", value = booking.paymentMethod)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (booking.isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (booking.isPaid) Icons.Filled.CheckCircle else Icons.Filled.Payments,
                    contentDescription = null,
                    tint = if (booking.isPaid) Color(0xFF15803D) else Color(0xFFB45309),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (booking.isPaid) "Đã thanh toán đầy đủ" else "Chưa thanh toán",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (booking.isPaid) Color(0xFF15803D) else Color(0xFFB45309)
                )
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    SectionCard(title = "GHI CHÚ") {
        Text(
            text = notes,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun BookingActions(
    booking: BookingItem,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit = {},
    onPaymentConfirm: () -> Unit = {}
) {
    when (booking.status) {
        BookingStatus.PENDING -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = "XÁC NHẬN LỊCH ĐẶT")
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48))
                ) {
                    Icon(
                        imageVector = Icons.Filled.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = "TỪ CHỐI")
                }
            }
        }
        BookingStatus.CONFIRMED -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("DỜI LỊCH")
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48))
                    ) {
                        Text("HỦY LỊCH")
                    }
                }
                if (!booking.isPaid) {
                    Button(
                        onClick = onPaymentConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Icon(Icons.Filled.Payments, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("XÁC NHẬN THANH TOÁN")
                    }
                }
            }
        }
        BookingStatus.COMPLETED -> {
            if (!booking.isPaid) {
                Button(
                    onClick = onPaymentConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Icon(Icons.Filled.Payments, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("XÁC NHẬN THANH TOÁN")
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun CustomerProfileCard(booking: BookingItem, onMessage: () -> Unit) {
    SectionCard(title = "HỒ SƠ KHÁCH HÀNG") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = booking.customer.name.first().uppercaseChar().toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = booking.customer.name,
                        style = MaterialTheme.typography.titleMedium,
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
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
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

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))

        InfoRow(icon = Icons.Filled.Phone, label = "Điện thoại", value = booking.customer.phone)
        InfoRow(icon = Icons.Filled.Email, label = "Email", value = booking.customer.email)

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onMessage,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("NHẮN TIN")
            }
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("GỌI ĐIỆN")
            }
        }
    }
}

@Composable
private fun CustomerStatsCard(booking: BookingItem) {
    SectionCard(title = "THỐNG KÊ ĐẶT SÂN") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "TỔNG ĐẶT SÂN",
                value = "${booking.customer.totalBookings} lần",
                color = MaterialTheme.colorScheme.primary
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            StatItem(
                label = "TỔNG CHI TIÊU",
                value = formatVnd(booking.customer.totalSpend) + "đ",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun HistoryEventRow(event: BookingHistoryEvent, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, bottom = if (!isLast) 8.dp else 0.dp)
        ) {
            Text(
                text = event.timestamp,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = event.action.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = event.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "bởi ${event.author}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
                letterSpacing = 0.8.sp
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun PaymentRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.secondary,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
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
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatVnd(amount: Long): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)
