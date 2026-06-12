package com.sportmanagement.manager.ui.screens.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.domain.model.BookingStatus
import com.sportmanagement.manager.ui.state.DayChipData
import com.sportmanagement.manager.ui.state.PitchFilterData
import com.sportmanagement.manager.ui.state.formatWeekRangeLabel
import com.sportmanagement.manager.ui.viewmodel.BookingsViewModel
import com.sportmanagement.manager.ui.theme.Amber
import com.sportmanagement.manager.ui.theme.AmberContainer
import com.sportmanagement.manager.ui.theme.AmberText

private val BookingStatus.accent: Color
    get() = when (this) {
        BookingStatus.PENDING -> Amber
        BookingStatus.CONFIRMED -> Color(0xFF15803D)
        BookingStatus.COMPLETED -> Color(0xFF94A3B8)
        BookingStatus.CANCELLED -> Color(0xFFE11D48)
    }

private val BookingStatus.badgeBg: Color
    get() = when (this) {
        BookingStatus.PENDING -> AmberContainer
        BookingStatus.CONFIRMED -> Color(0xFFE7F5EC)
        BookingStatus.COMPLETED -> Color(0xFFE2E8F0)
        BookingStatus.CANCELLED -> Color(0xFFFFE4E6)
    }

private val BookingStatus.badgeText: Color
    get() = when (this) {
        BookingStatus.PENDING -> AmberText
        BookingStatus.CONFIRMED -> Color(0xFF15803D)
        BookingStatus.COMPLETED -> Color(0xFF64748B)
        BookingStatus.CANCELLED -> Color(0xFFBE123C)
    }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BookingsScreen(
    padding: PaddingValues,
    onBookingClick: (BookingItem) -> Unit = {},
    onAddBooking: () -> Unit = {},
    viewModel: BookingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BookingHeader(
                        weekRangeLabel = formatWeekRangeLabel(uiState.visibleWeekStartDate),
                        onCalendarClick = { showDatePicker = true }
                    )
                    WeekNavigator(
                        onPreviousWeek = viewModel::onPreviousWeek,
                        onNextWeek = viewModel::onNextWeek,
                        weekRangeLabel = formatWeekRangeLabel(uiState.visibleWeekStartDate)
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(uiState.dayChips) { index, chip ->
                        DayChipView(
                            chip = chip,
                            onClick = { viewModel.onDaySelected(index) }
                        )
                    }
                }

                if (showDatePicker) {
                    val selectedMillis = uiState.dayChips.firstOrNull { it.isSelected }?.isoDate?.let { dateIso ->
                        runCatching {
                            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }.parse(dateIso)?.time
                        }.getOrNull()
                    }
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedMillis)

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let(viewModel::onCalendarDateSelected)
                                    showDatePicker = false
                                }
                            ) { Text("Chọn") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(uiState.pitchFilters) { index, chip ->
                        FilterChipView(
                            chip = chip,
                            onClick = { viewModel.onPitchFilterSelected(index) }
                        )
                    }
                }
            }

            items(uiState.filteredBookings) { booking ->
                TimelineRow(
                    booking = booking,
                    onClick = { onBookingClick(booking) },
                    onConfirm = { viewModel.onConfirmBooking(booking.id) },
                    onCancel = { viewModel.onRequestCancel(booking.id) }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }

        FloatingActionButton(
            onClick = onAddBooking,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = padding.calculateBottomPadding() + 16.dp, end = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Thêm lịch",
                modifier = Modifier.size(26.dp)
            )
        }
    }

    if (uiState.showCancelDialog) {
        CancelBookingDialog(
            reason = uiState.cancelReasonDraft,
            onReasonChanged = viewModel::onCancelReasonChanged,
            onConfirm = viewModel::onConfirmCancel,
            onDismiss = viewModel::onDismissCancelDialog
        )
    }
}

@Composable
private fun CancelBookingDialog(
    reason: String,
    onReasonChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hủy lịch đặt sân") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Vui lòng ghi nhận lý do hủy để thông báo cho khách hàng.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChanged,
                    label = { Text("Lý do hủy") },
                    placeholder = { Text("Ví dụ: Sân đang bảo trì khẩn cấp...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE11D48)
                )
            ) { Text("Xác nhận hủy") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Quay lại") }
        }
    )
}

@Composable
private fun BookingHeader(
    weekRangeLabel: String,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Lịch Đặt Sân",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onCalendarClick) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "Chọn ngày",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    Text(
        text = weekRangeLabel,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun WeekNavigator(
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    weekRangeLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onPreviousWeek) { Text("<") }
        Text(
            text = weekRangeLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedButton(onClick = onNextWeek) { Text(">") }
    }
}

@Composable
private fun DayChipView(chip: DayChipData, onClick: () -> Unit) {
    val bg = if (chip.isSelected) MaterialTheme.colorScheme.primary else Color.White
    val textColor = if (chip.isSelected) Color.White else MaterialTheme.colorScheme.outline
    val numberColor = when {
        chip.isSelected -> Color.White
        chip.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onBackground
    }
    val borderWidth = when {
        chip.isSelected -> 0.dp
        chip.isToday -> 1.5.dp
        else -> 1.dp
    }
    val borderColor = when {
        chip.isSelected -> Color.Transparent
        chip.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chip.dayLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = chip.dayNumber,
            fontSize = 18.sp,
            fontWeight = if (chip.isToday) FontWeight.ExtraBold else FontWeight.Bold,
            color = numberColor
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(
                    if (chip.isToday) {
                        if (chip.isSelected) Color.White else MaterialTheme.colorScheme.primary
                    } else Color.Transparent
                )
        )
    }
}

@Composable
private fun FilterChipView(chip: PitchFilterData, onClick: () -> Unit) {
    val bg = if (chip.isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.White
    val border = if (chip.isSelected)
        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (chip.isSelected)
        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = chip.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun TimelineRow(
    booking: BookingItem,
    onClick: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(56.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = booking.createdAtTime.ifBlank { booking.startTime },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )
            if (booking.showLive) {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "LIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        BookingCard(
            booking = booking,
            modifier = Modifier.weight(1f),
            onClick = onClick,
            onConfirm = onConfirm,
            onCancel = onCancel
        )
    }
}

@Composable
private fun BookingCard(
    booking: BookingItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(booking.status.accent)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(booking.status.badgeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = booking.status.badgeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = booking.status.badgeText
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = booking.customer.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${booking.startTime} - ${booking.endTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (booking.totalPrice > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${booking.totalPrice / 1000}k",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                when (booking.status) {
                    BookingStatus.CONFIRMED -> {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE11D48)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE11D48))
                        ) { Text(text = "HỦY SÂN") }
                    }
                    BookingStatus.PENDING -> {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) { Text(text = "XÁC NHẬN NGAY") }
                    }
                    BookingStatus.COMPLETED -> {
                        if (booking.isPaid) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Đã thanh toán",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
