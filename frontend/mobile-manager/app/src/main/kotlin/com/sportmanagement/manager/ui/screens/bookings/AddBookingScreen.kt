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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.manager.ui.state.BookingsUiState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookingScreen(
    uiState: BookingsUiState,
    onBackClick: () -> Unit,
    onFieldSelected: (Int) -> Unit,
    onCourtSelected: (courtId: Int, courtCode: String) -> Unit,
    onDateChanged: (String) -> Unit,
    onTimeSlotTapped: (String) -> Unit,
    onCustomerNameChanged: (String) -> Unit,
    onCustomerPhoneChanged: (String) -> Unit,
    onDepositChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        val day = "%02d".format(cal.get(Calendar.DAY_OF_MONTH))
                        val month = "%02d".format(cal.get(Calendar.MONTH) + 1)
                        val year = cal.get(Calendar.YEAR)
                        onDateChanged("$day/$month/$year")
                    }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Thêm lịch đặt sân",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            modifier = Modifier.shadow(4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FormSectionCard(title = "CHỌN SÂN", icon = Icons.Filled.SportsSoccer) {
                    // Field selector — only shown when manager has multiple fields
                    if (uiState.newBookingFields.size > 1) {
                        Text(
                            text = "Cơ sở sân",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.newBookingFields) { field ->
                                val isSelected = field.fieldId == uiState.newBookingSelectedFieldId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceContainerLowest
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onFieldSelected(field.fieldId) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = field.fieldName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Text(
                        text = "Sân con",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(8.dp))
                    if (uiState.newBookingCourts.isEmpty()) {
                        Text(
                            text = if (uiState.newBookingSelectedFieldId == null) "Đang tải..."
                                   else "Không có sân con nào",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.newBookingCourts) { court ->
                                val isSelected = court.courtId == uiState.newBookingCourtId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceContainerLowest
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onCourtSelected(court.courtId, court.courtCode) }
                                        .padding(horizontal = 18.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = court.courtCode,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                FormSectionCard(title = "THỜI GIAN", icon = Icons.Filled.CalendarMonth) {
                    // Date picker button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (uiState.newBookingDate.isBlank()) "Chọn ngày" else uiState.newBookingDate,
                                color = if (uiState.newBookingDate.isBlank())
                                    MaterialTheme.colorScheme.outline
                                else MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp
                            )
                        }
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Slot selection hint
                    val startMin = if (uiState.newBookingStart.isNotBlank())
                        timeToMinutes(uiState.newBookingStart) else -1
                    val endMin = if (uiState.newBookingEnd.isNotBlank())
                        timeToMinutes(uiState.newBookingEnd) else -1
                    val maxEnd = if (startMin >= 0 && endMin < 0)
                        slotMaxEndMin(startMin, uiState.newBookingBookedRanges) else 22 * 60

                    val phaseHint = when {
                        uiState.newBookingStart.isBlank() -> "Chọn giờ bắt đầu"
                        uiState.newBookingEnd.isBlank() ->
                            "Chọn giờ kết thúc (trước ${slotFormatMinutes(maxEnd)})"
                        else -> "Đã chọn: ${uiState.newBookingStart} – ${uiState.newBookingEnd}"
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = if (uiState.newBookingEnd.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = phaseHint,
                            fontSize = 12.sp,
                            color = if (uiState.newBookingEnd.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            fontWeight = if (uiState.newBookingEnd.isNotBlank())
                                FontWeight.SemiBold else FontWeight.Normal
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Loading indicator
                    if (uiState.newBookingIsLoadingSlots) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        LegendDot(color = MaterialTheme.colorScheme.errorContainer, label = "Đã đặt")
                        LegendDot(color = MaterialTheme.colorScheme.primary, label = "Chọn")
                        LegendDot(color = MaterialTheme.colorScheme.primaryContainer, label = "Trong khung")
                    }

                    // 4-column slot grid
                    val slotRows = ALL_TIME_SLOTS.chunked(4)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        slotRows.forEach { rowSlots ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowSlots.forEach { slot ->
                                    val slotMin = timeToMinutes(slot)
                                    val booked = isSlotBooked(slot, uiState.newBookingBookedRanges)
                                    val isStart = slot == uiState.newBookingStart
                                    val inRange = startMin >= 0 && endMin >= 0 &&
                                        slotMin > startMin && slotMin < endMin
                                    val disabledByRule = !isStart && !booked &&
                                        startMin >= 0 && endMin < 0 &&
                                        (slotMin <= startMin || slotMin > maxEnd)
                                    SlotCell(
                                        time = slot,
                                        isStart = isStart,
                                        inRange = inRange,
                                        booked = booked,
                                        disabled = disabledByRule,
                                        onClick = { onTimeSlotTapped(slot) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(4 - rowSlots.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            item {
                FormSectionCard(title = "THÔNG TIN KHÁCH HÀNG", icon = Icons.Filled.Person) {
                    OutlinedTextField(
                        value = uiState.newBookingCustomerName,
                        onValueChange = onCustomerNameChanged,
                        label = { Text("Họ và tên") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Filled.Person, null, modifier = Modifier.size(18.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = uiState.newBookingCustomerPhone,
                        onValueChange = onCustomerPhoneChanged,
                        label = { Text("Số điện thoại") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Icon(Icons.Filled.Phone, null, modifier = Modifier.size(18.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                }
            }

            item {
                FormSectionCard(title = "THANH TOÁN", icon = Icons.Filled.Payments) {
                    val durationH = if (uiState.newBookingStart.isNotBlank() && uiState.newBookingEnd.isNotBlank()) {
                        val sMin = timeToMinutes(uiState.newBookingStart)
                        val eMin = timeToMinutes(uiState.newBookingEnd)
                        if (eMin > sMin) (eMin - sMin) / 60.0 else 0.0
                    } else 0.0
                    val pricePerHour = 300_000L
                    val total = (pricePerHour * durationH).toLong()

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PriceLine("Đơn giá", "300,000đ/h")
                        PriceLine("Thời gian", String.format("%.1fh", durationH))
                        PriceLine(
                            "Tổng dự tính",
                            "${formatVndFmt(total)}đ",
                            bold = true,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = uiState.newBookingDeposit,
                        onValueChange = onDepositChanged,
                        label = { Text("Đặt cọc (đ)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(Icons.Filled.Payments, null, modifier = Modifier.size(18.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.newBookingNotes,
                    onValueChange = onNotesChanged,
                    label = { Text("Ghi chú (tùy chọn)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            }

            item {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = uiState.newBookingSelectedFieldId != null &&
                        uiState.newBookingCourtId != null &&
                        uiState.newBookingDate.isNotBlank() &&
                        uiState.newBookingStart.isNotBlank() &&
                        uiState.newBookingEnd.isNotBlank() &&
                        uiState.newBookingCustomerName.isNotBlank() &&
                        uiState.newBookingCustomerPhone.isNotBlank()
                ) {
                    Text(
                        text = "XÁC NHẬN ĐẶT SÂN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SlotCell(
    time: String,
    isStart: Boolean,
    inRange: Boolean,
    booked: Boolean,
    disabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isStart -> MaterialTheme.colorScheme.primary
        inRange -> MaterialTheme.colorScheme.primaryContainer
        booked -> MaterialTheme.colorScheme.errorContainer
        disabled -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val textColor = when {
        isStart -> Color.White
        booked -> MaterialTheme.colorScheme.error
        disabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        inRange -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onBackground
    }
    val borderColor = when {
        isStart -> MaterialTheme.colorScheme.primary
        inRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        booked -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val canTap = !booked && !disabled
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(7.dp))
            .then(if (canTap) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            fontSize = 11.sp,
            fontWeight = if (isStart) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 0.8.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun PriceLine(
    label: String,
    value: String,
    bold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.secondary
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

private fun slotFormatMinutes(totalMin: Int): String =
    "%02d:%02d".format(totalMin / 60, totalMin % 60)

private fun formatVndFmt(amount: Long): String =
    java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(amount)
