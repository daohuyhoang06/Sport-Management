package com.sportmanagement.manager.ui.screens.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.ui.state.BookingsUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookingSheetRoot(
    state: BookingsUiState,
    onDateChanged: (String) -> Unit,
    onStartChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = remember(state.editDate) {
            parseDdMmYyyyToMillis(state.editDate) ?: System.currentTimeMillis()
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateChanged(formatMillisToDdMmYyyy(millis))
                    }
                    showDatePicker = false
                }) { Text("Chọn ngày") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DỜI LỊCH",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = state.editPitchName.ifBlank { "Booking" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${state.editCourtCode} • ${state.editCourtName.ifBlank { "Sân con" }}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            item {
                BookingSummaryCard(
                    booking = state.toBookingPreview(),
                    selectedDate = state.editDate,
                    selectedStart = state.editStart,
                    selectedEnd = state.editEnd
                )
            }

            item {
                SectionCard(title = "CHỌN NGÀY", icon = Icons.Filled.CalendarMonth) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (state.editDate.isBlank()) "Chọn ngày" else state.editDate,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Chỉ hiển thị khung giờ trống trên sân hiện tại.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            item {
                SectionCard(title = "KHUNG GIỜ TRỐNG", icon = Icons.Filled.Schedule) {
                    val startMin = if (state.editStart.isNotBlank()) timeToMinutes(state.editStart) else -1
                    val endMin = if (state.editEnd.isNotBlank()) timeToMinutes(state.editEnd) else -1
                    val maxEnd = if (startMin >= 0 && endMin < 0) slotMaxEndMin(startMin, state.editBookedRanges) else 22 * 60
                    val availableStartSlots = remember(state.editBookedRanges) {
                        availableStartSlots(state.editBookedRanges)
                    }
                    val availableEndSlots = remember(startMin, state.editBookedRanges) {
                        if (startMin >= 0) availableEndSlots(startMin, state.editBookedRanges) else emptyList()
                    }

                    TimeRangeSummary(
                        startTime = state.editStart,
                        endTime = state.editEnd,
                        maxEnd = maxEnd
                    )
                    Spacer(Modifier.height(10.dp))

                    if (state.editIsLoadingSlots) {
                        androidx.compose.material3.LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                    }

                    TimeRailSection(
                        title = "1. Chọn giờ bắt đầu",
                        subtitle = if (state.editDate.isBlank()) "Chọn ngày trước" else "Chỉ gồm các slot còn trống",
                        icon = Icons.Filled.AccessTime,
                        chips = availableStartSlots,
                        selectedValue = state.editStart,
                        emptyText = if (state.editIsLoadingSlots) "Đang tải khung giờ..." else "Không còn slot trống",
                        onChipClick = onStartChanged
                    )

                    Spacer(Modifier.height(12.dp))

                    TimeRailSection(
                        title = "2. Chọn giờ kết thúc",
                        subtitle = if (state.editStart.isBlank()) "Chọn giờ bắt đầu trước" else "Kết thúc phải sau giờ bắt đầu",
                        icon = Icons.Filled.CheckCircle,
                        chips = availableEndSlots,
                        selectedValue = state.editEnd,
                        emptyText = if (state.editStart.isBlank()) "Chưa có giờ bắt đầu" else "Không có giờ kết thúc phù hợp",
                        enabled = state.editStart.isNotBlank(),
                        onChipClick = onEndChanged
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Close, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Hủy")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = state.editDate.isNotBlank() &&
                            state.editStart.isNotBlank() &&
                            state.editEnd.isNotBlank() &&
                            !state.editIsLoadingSlots
                    ) {
                        Text("Lưu dời lịch", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun BookingSummaryCard(
    booking: BookingItem,
    selectedDate: String,
    selectedStart: String,
    selectedEnd: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryLine("Hiện tại", "${booking.startTime} - ${booking.endTime}")
            SummaryLine("Ngày", booking.date.ifBlank { "Chưa xác định" })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SummaryLine(
                "Sau khi dời",
                if (selectedDate.isBlank() || selectedStart.isBlank() || selectedEnd.isBlank()) "Chưa chọn"
                else "$selectedDate • $selectedStart - $selectedEnd",
                valueColor = MaterialTheme.colorScheme.primary
            )
            SummaryLine(
                "Tổng thời gian",
                if (selectedStart.isBlank() || selectedEnd.isBlank()) "—"
                else formatDurationLabel(selectedStart, selectedEnd),
                valueColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
        Text(text = value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            content()
        }
    }
}

@Composable
private fun TimeRangeSummary(
    startTime: String,
    endTime: String,
    maxEnd: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniTimeCard(
            label = "Bắt đầu",
            value = if (startTime.isBlank()) "Chưa chọn" else startTime,
            active = startTime.isNotBlank() && endTime.isBlank(),
            modifier = Modifier.weight(1f)
        )
        MiniTimeCard(
            label = "Kết thúc",
            value = if (endTime.isBlank()) "Chưa chọn" else endTime,
            active = endTime.isNotBlank(),
            modifier = Modifier.weight(1f)
        )
        MiniTimeCard(
            label = "Tối đa",
            value = slotFormatMinutes(maxEnd),
            active = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniTimeCard(
    label: String,
    value: String,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.White)
            .border(
                1.dp,
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TimeRailSection(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    chips: List<String>,
    selectedValue: String,
    emptyText: String,
    onChipClick: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
        }

        if (!enabled || chips.isEmpty()) {
            EmptyTimeRail(text = emptyText)
            return
        }

        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chips) { time ->
                TimeChip(
                    time = time,
                    isSelected = time == selectedValue,
                    onClick = { onChipClick(time) }
                )
            }
        }
    }
}

@Composable
private fun TimeChip(
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(999.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = time,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun EmptyTimeRail(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Text(text = text, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
    }
}

private val ALL_TIME_SLOTS: List<String> = buildList {
    var minutes = 6 * 60
    while (minutes <= 22 * 60) {
        add("%02d:%02d".format(minutes / 60, minutes % 60))
        minutes += 30
    }
}

private fun timeToMinutes(time: String): Int {
    val parts = time.split(":")
    return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
}

private fun isSlotBooked(slotTime: String, ranges: List<Pair<Int, Int>>): Boolean {
    val m = timeToMinutes(slotTime)
    val end = m + 30
    return ranges.any { (bs, be) -> m < be && end > bs }
}

private fun slotMaxEndMin(startMin: Int, ranges: List<Pair<Int, Int>>): Int =
    ranges.filter { (bs, _) -> bs > startMin }.minOfOrNull { (bs, _) -> bs } ?: (22 * 60)

private fun availableStartSlots(ranges: List<Pair<Int, Int>>): List<String> =
    ALL_TIME_SLOTS.filterNot { isSlotBooked(it, ranges) }

private fun availableEndSlots(startMin: Int, ranges: List<Pair<Int, Int>>): List<String> {
    val maxEnd = slotMaxEndMin(startMin, ranges)
    return ALL_TIME_SLOTS.filter { slot ->
        val min = timeToMinutes(slot)
        min > startMin && min <= maxEnd && !isSlotBooked(slot, ranges)
    }
}

private fun formatDurationLabel(startTime: String, endTime: String): String {
    val startMin = timeToMinutes(startTime)
    val endMin = timeToMinutes(endTime)
    val duration = (endMin - startMin).coerceAtLeast(0)
    val hours = duration / 60
    val minutes = duration % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}p"
        hours > 0 -> "${hours}h"
        else -> "${minutes}p"
    }
}

private fun slotFormatMinutes(totalMin: Int): String =
    "%02d:%02d".format(totalMin / 60, totalMin % 60)

private fun parseDdMmYyyyToMillis(date: String): Long? {
    if (date.isBlank()) return null
    return runCatching {
        SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
            .parse(date)
            ?.time
    }.getOrNull()
}

private fun formatMillisToDdMmYyyy(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return "%02d/%02d/%04d".format(
        cal.get(Calendar.DAY_OF_MONTH),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.YEAR)
    )
}

private fun BookingItem.toBookingPreview(): BookingItem = this

private fun BookingsUiState.toBookingPreview(): BookingItem = BookingItem(
    id = editTargetId,
    pitchName = editPitchName,
    courtCode = editCourtCode,
    courtName = editCourtName,
    customer = com.sportmanagement.manager.domain.model.BookingCustomer(
        id = "0",
        name = editCustomerName,
        phone = editCustomerPhone,
        email = "",
        avatarUrl = null,
        totalBookings = 0,
        totalSpend = 0L,
        memberSince = ""
    ),
    date = editDate,
    dayOfWeek = "",
    startTime = editStart,
    endTime = editEnd,
    durationMinutes = 0,
    pricePerHour = 0L,
    totalPrice = 0L,
    status = com.sportmanagement.manager.domain.model.BookingStatus.CONFIRMED,
    paymentStatus = "",
    isPaid = true
)
