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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.manager.ui.state.BookingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookingScreen(
    uiState: BookingsUiState,
    onBackClick: () -> Unit,
    onCourtCodeChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onStartChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onCustomerNameChanged: (String) -> Unit,
    onCustomerPhoneChanged: (String) -> Unit,
    onDepositChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    val courts = listOf("A1", "A2", "B1", "B2", "C1")
    val timeSlots = listOf("06:00", "07:00", "07:30", "08:00", "09:00",
        "10:00", "14:00", "15:00", "15:30", "16:00",
        "17:00", "17:30", "18:00", "18:30", "19:00", "20:00", "21:00")
    var selectedStartSlot by remember { mutableIntStateOf(10) }
    var selectedEndSlot by remember { mutableIntStateOf(12) }

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
                    Text(
                        text = "Sân con",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(courts) { court ->
                            val isSelected = court == uiState.newBookingCourtCode
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
                                    .clickable { onCourtCodeChanged(court) }
                                    .padding(horizontal = 18.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = court,
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

            item {
                FormSectionCard(title = "THỜI GIAN", icon = Icons.Filled.CalendarMonth) {
                    OutlinedTextField(
                        value = uiState.newBookingDate,
                        onValueChange = onDateChanged,
                        label = { Text("Ngày (dd/mm/yyyy)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Khung giờ bắt đầu",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(timeSlots) { index, slot ->
                            val isSelected = index == selectedStartSlot
                            TimeSlotChip(
                                time = slot,
                                isSelected = isSelected,
                                onClick = {
                                    selectedStartSlot = index
                                    onStartChanged(slot)
                                    if (index >= selectedEndSlot) {
                                        val nextIdx = minOf(index + 2, timeSlots.lastIndex)
                                        selectedEndSlot = nextIdx
                                        onEndChanged(timeSlots[nextIdx])
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Khung giờ kết thúc",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(timeSlots) { index, slot ->
                            val isSelected = index == selectedEndSlot
                            val enabled = index > selectedStartSlot
                            TimeSlotChip(
                                time = slot,
                                isSelected = isSelected,
                                enabled = enabled,
                                onClick = {
                                    selectedEndSlot = index
                                    onEndChanged(slot)
                                }
                            )
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
                    val startIdx = timeSlots.indexOf(uiState.newBookingStart).takeIf { it >= 0 } ?: 0
                    val endIdx = timeSlots.indexOf(uiState.newBookingEnd).takeIf { it >= 0 } ?: 0
                    val durationH = if (endIdx > startIdx) (endIdx - startIdx) * 0.5 else 1.5
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
                    enabled = uiState.newBookingCustomerName.isNotBlank() &&
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
private fun TimeSlotChip(
    time: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    !enabled -> MaterialTheme.colorScheme.surfaceContainerLowest
                    else -> Color.White
                }
            )
            .border(
                1.dp,
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    !enabled -> MaterialTheme.colorScheme.surfaceContainer
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
                RoundedCornerShape(8.dp)
            )
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = time,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> Color.White
                !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.onBackground
            }
        )
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

private fun formatVndFmt(amount: Long): String =
    java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(amount)
