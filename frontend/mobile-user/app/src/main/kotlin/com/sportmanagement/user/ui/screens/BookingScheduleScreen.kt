package com.sportmanagement.user.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.CourtRow
import com.sportmanagement.user.domain.model.SlotStatus
import com.sportmanagement.user.domain.model.TimeSlot
import com.sportmanagement.user.ui.theme.AppAccentCitrus
import com.sportmanagement.user.ui.theme.SportUserTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BookingBackground = Color(0xFFE2EFE8)
private val BookingNoticeBackground = Color(0xFFE6F2EC)
private val BookingGridHeaderBlue = Color(0xFFAEDCEF)
private val BookingLockedGray = Color(0xFFACACAC)
private val BookingBookedCellRed = Color(0xFFB71C1C)
private const val DefaultPricePerHalfHour = 100_000

private data class BookingSelectionSummary(
    val courtName: String,
    val startTimeLabel: String,
    val endTimeLabel: String,
    val totalMinutes: Int,
    val totalPrice: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScheduleScreen(
    scheduleData: BookingScheduleData,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDateText by rememberSaveable { mutableStateOf(scheduleData.selectedDate) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var cellWidthValue by rememberSaveable { mutableFloatStateOf(46f) }
    var selectedSlots by remember { mutableStateOf(setOf<String>()) }
    val selectionSummary = remember(selectedSlots, scheduleData) {
        buildSelectionSummary(scheduleData, selectedSlots)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        androidx.compose.material3.Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = BookingBackground,
            bottomBar = {
                BookingBottomActionBar(
                    sliderValue = cellWidthValue,
                    onSliderChange = { cellWidthValue = it },
                    summary = selectionSummary,
                    onNextClick = onNextClick
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    BookingHeaderSection(
                        selectedDateText = selectedDateText,
                        onBackClick = onBackClick,
                        onDateClick = { showDatePicker = true }
                    )
                }
                item {
                    BookingNoteSection()
                }
                item {
                    BookingGridSection(
                        scheduleData = scheduleData,
                        cellWidthValue = cellWidthValue,
                        selectedSlots = selectedSlots,
                        onSlotToggle = { slotKey ->
                            selectedSlots = toggleSingleCourtSelection(
                                current = selectedSlots,
                                slotKey = slotKey
                            )
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseDateToMillis(selectedDateText) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDateText = formatDateFromMillis(millis)
                        }
                        showDatePicker = false
                    }
                ) { Text(stringResource(R.string.booking_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.booking_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
private fun BookingHeaderSection(
    selectedDateText: String,
    onBackClick: () -> Unit,
    onDateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.booking_back_content_description),
                    tint = Color.White
                )
            }
            Text(
                text = stringResource(R.string.booking_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = onDateClick,
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDateText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.booking_calendar_content_description),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BookingLegendItem(SlotStatus.AVAILABLE, stringResource(R.string.booking_status_available))
            BookingLegendItem(SlotStatus.BOOKED, stringResource(R.string.booking_status_booked))
            BookingLegendItem(SlotStatus.LOCKED, stringResource(R.string.booking_status_locked))
        }

        Text(
            text = stringResource(R.string.booking_view_field_and_price),
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.titleMedium,
            textDecoration = TextDecoration.Underline,
            color = AppAccentCitrus,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BookingLegendItem(status: SlotStatus, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BookingSlotCell(
            status = status,
            modifier = Modifier.size(22.dp),
            iconSize = 12.dp,
            borderColor = Color.Transparent
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
private fun BookingNoteSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookingNoticeBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = stringResource(R.string.booking_notice_title),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFE65100),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.booking_notice_deposit),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.booking_notice_student),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.booking_notice_adult),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BookingGridSection(
    scheduleData: BookingScheduleData,
    cellWidthValue: Float,
    selectedSlots: Set<String>,
    onSlotToggle: (String) -> Unit
) {
    val horizontalScroll = rememberScrollState()
    val cellWidth = cellWidthValue.dp
    val cellHeight = 56.dp
    val leftLabelWidth = 80.dp
    val borderColor = Color(0xFF8E8E8E).copy(alpha = 0.35f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookingGridHeaderBlue)
        ) {
            Box(
                modifier = Modifier
                    .width(leftLabelWidth)
                    .height(56.dp)
                    .border(1.dp, borderColor)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(horizontalScroll)
                    .height(56.dp)
            ) {
                scheduleData.timeHeaders.forEach { header ->
                    val shouldShowLabel = when {
                        cellWidthValue >= 28f -> true
                        cellWidthValue >= 16f -> header.endsWith(":00")
                        else -> header == "0:00" || header == "12:00" || header == "24:00"
                    }
                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxSize()
                            .border(1.dp, borderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (shouldShowLabel) {
                            Text(
                                text = header,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        scheduleData.courts.forEachIndexed { courtIndex, court ->
            BookingCourtRow(
                row = court,
                courtIndex = courtIndex,
                leftLabelWidth = leftLabelWidth,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                borderColor = borderColor,
                horizontalScroll = horizontalScroll,
                selectedSlots = selectedSlots,
                onSlotToggle = { slotKey ->
                    onSlotToggle(slotKey)
                }
            )
        }
    }
}

@Composable
private fun BookingCourtRow(
    row: CourtRow,
    courtIndex: Int,
    leftLabelWidth: Dp,
    cellWidth: Dp,
    cellHeight: Dp,
    borderColor: Color,
    horizontalScroll: androidx.compose.foundation.ScrollState,
    selectedSlots: Set<String>,
    onSlotToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookingNoticeBackground)
    ) {
        Box(
            modifier = Modifier
                .width(leftLabelWidth)
                .height(cellHeight)
                .border(1.dp, borderColor)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = row.courtName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
                .height(cellHeight)
        ) {
            row.slots.forEachIndexed { slotIndex, slot ->
                val slotKey = "$courtIndex:$slotIndex"
                BookingGridCell(
                    slot = slot,
                    isSelected = selectedSlots.contains(slotKey),
                    onToggle = { onSlotToggle(slotKey) },
                    cellWidth = cellWidth,
                    borderColor = borderColor
                )
            }
        }
    }
}

@Composable
private fun BookingGridCell(
    slot: TimeSlot,
    isSelected: Boolean,
    onToggle: () -> Unit,
    cellWidth: Dp,
    borderColor: Color
) {
    val displayStatus = if (isSelected) SlotStatus.SELECTED else slot.status
    val isSelectable = slot.status == SlotStatus.AVAILABLE || isSelected

    BookingSlotCell(
        status = displayStatus,
        modifier = Modifier
            .width(cellWidth)
            .fillMaxSize()
            .border(1.dp, borderColor)
            .then(
                if (isSelectable) {
                    Modifier.clickable(onClick = onToggle)
                } else {
                    Modifier
                }
            ),
        iconSize = (cellWidth * 0.55f),
        borderColor = Color.Transparent
    )
}

@Composable
private fun BookingSlotCell(
    status: SlotStatus,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val background = when (status) {
        SlotStatus.AVAILABLE -> Color.White
        SlotStatus.BOOKED -> BookingBookedCellRed
        SlotStatus.LOCKED -> BookingLockedGray
        SlotStatus.EVENT -> BookingLockedGray
        SlotStatus.SELECTED -> MaterialTheme.colorScheme.primaryContainer
    }

    Box(
        modifier = modifier
            .background(background)
            .border(1.dp, borderColor),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            SlotStatus.BOOKED -> Unit

            SlotStatus.EVENT -> Unit

            SlotStatus.SELECTED -> Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(iconSize)
            )

            else -> Unit
        }
    }
}

@Composable
private fun BookingBottomActionBar(
    sliderValue: Float,
    onSliderChange: (Float) -> Unit,
    summary: BookingSelectionSummary?,
    onNextClick: () -> Unit
) {
    var showSelectedRange by rememberSaveable(
        summary?.courtName,
        summary?.startTimeLabel,
        summary?.endTimeLabel
    ) { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = onSliderChange,
                    valueRange = 4f..68f,
                    modifier = Modifier
                        .width(170.dp)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }

        if (summary != null) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    TextButton(
                        onClick = { showSelectedRange = !showSelectedRange },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = if (showSelectedRange) {
                                Icons.Default.KeyboardArrowDown
                            } else {
                                Icons.Default.KeyboardArrowUp
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                if (showSelectedRange) {
                                    R.string.booking_hide_selected_range
                                } else {
                                    R.string.booking_show_selected_range
                                }
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    if (showSelectedRange) {
                        Text(
                            text = stringResource(
                                R.string.booking_selected_range_format,
                                summary.courtName,
                                summary.startTimeLabel,
                                summary.endTimeLabel
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                R.string.booking_total_hours_format,
                                formatDurationCompact(summary.totalMinutes)
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(
                                R.string.booking_total_price_format,
                                formatCurrencyVnd(summary.totalPrice)
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNextClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppAccentCitrus),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.booking_next),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            Button(
                onClick = onNextClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccentCitrus),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.booking_next),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun toggleSingleCourtSelection(current: Set<String>, slotKey: String): Set<String> {
    val target = parseSlotKey(slotKey) ?: return current
    val currentCourt = current.firstNotNullOfOrNull { parseSlotKey(it)?.first }
    val workingSet = if (currentCourt != null && currentCourt != target.first) {
        emptySet()
    } else {
        current
    }

    return if (workingSet.contains(slotKey)) {
        workingSet - slotKey
    } else {
        workingSet + slotKey
    }
}

private fun buildSelectionSummary(
    scheduleData: BookingScheduleData,
    selectedSlots: Set<String>
): BookingSelectionSummary? {
    if (selectedSlots.isEmpty()) return null

    val parsed = selectedSlots
        .mapNotNull { parseSlotKey(it) }
        .sortedBy { it.second }
    if (parsed.isEmpty()) return null

    val courtIndex = parsed.first().first
    val court = scheduleData.courts.getOrNull(courtIndex) ?: return null
    val selectedIndices = parsed.map { it.second }

    val startIndex = selectedIndices.minOrNull() ?: return null
    val endExclusiveIndex = (selectedIndices.maxOrNull() ?: return null) + 1
    val totalMinutes = selectedIndices.size * 30
    val unitPrice = resolveUnitPricePerHalfHour(scheduleData)

    return BookingSelectionSummary(
        courtName = court.courtName,
        startTimeLabel = scheduleData.timeHeaders.getOrElse(startIndex) { "0:00" },
        endTimeLabel = formatEndTime(scheduleData.timeHeaders, endExclusiveIndex),
        totalMinutes = totalMinutes,
        totalPrice = selectedIndices.size * unitPrice
    )
}

private fun resolveUnitPricePerHalfHour(scheduleData: BookingScheduleData): Int {
    val estimatedPrice = extractDigits(scheduleData.estimatedPrice)
    val baseSlots = scheduleData.selectedSlotCount
    return if (estimatedPrice != null && estimatedPrice > 0 && baseSlots > 0) {
        (estimatedPrice / baseSlots).coerceAtLeast(1)
    } else {
        DefaultPricePerHalfHour
    }
}

private fun formatEndTime(headers: List<String>, endExclusiveIndex: Int): String {
    if (endExclusiveIndex < headers.size) return headers[endExclusiveIndex]
    val baseIndex = endExclusiveIndex - 1
    val baseTime = headers.getOrNull(baseIndex) ?: return "24:00"
    val parts = baseTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return "24:00"
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val plusThirty = hour * 60 + minute + 30
    val endHour = (plusThirty / 60).coerceAtMost(24)
    val endMinute = plusThirty % 60
    return "%d:%02d".format(endHour, endMinute)
}

private fun parseSlotKey(slotKey: String): Pair<Int, Int>? {
    val split = slotKey.split(":")
    if (split.size != 2) return null
    val courtIndex = split[0].toIntOrNull() ?: return null
    val slotIndex = split[1].toIntOrNull() ?: return null
    return courtIndex to slotIndex
}

private fun formatDurationCompact(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%dh%02d".format(hours, minutes)
}

private fun formatCurrencyVnd(amount: Int): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)} đ"
}

private fun extractDigits(value: String): Int? {
    val digits = value.filter { it.isDigit() }
    if (digits.isEmpty()) return null
    return digits.toIntOrNull()
}

private fun parseDateToMillis(dateText: String): Long? {
    return runCatching {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        formatter.parse(dateText)?.time
    }.getOrNull()
}

private fun formatDateFromMillis(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
    return formatter.format(Date(millis))
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BookingScheduleScreenPreview() {
    val previewHeaders = (0..48).map { index ->
        val hour = index / 2
        val minute = if (index % 2 == 0) "00" else "30"
        "$hour:$minute"
    }

    SportUserTheme {
        BookingScheduleScreen(
            scheduleData = BookingScheduleData(
                selectedDate = "25/04/2026",
                timeHeaders = previewHeaders,
                courts = listOf(
                    CourtRow(
                        courtName = "Sân 1",
                        slots = previewHeaders.mapIndexed { index, label ->
                            val status = when (index) {
                                in 34..37 -> SlotStatus.BOOKED
                                in 38..41 -> SlotStatus.LOCKED
                                else -> SlotStatus.AVAILABLE
                            }
                            TimeSlot(label, status)
                        }
                    ),
                    CourtRow(
                        courtName = "Sân 2",
                        slots = previewHeaders.mapIndexed { index, label ->
                            val status = when (index) {
                                in 34..35 -> SlotStatus.BOOKED
                                in 10..15 -> SlotStatus.LOCKED
                                else -> SlotStatus.AVAILABLE
                            }
                            TimeSlot(label, status)
                        }
                    ),
                    CourtRow(
                        courtName = "Sân 3",
                        slots = previewHeaders.mapIndexed { index, label ->
                            val status = when (index) {
                                36 -> SlotStatus.BOOKED
                                in 0..15 -> SlotStatus.LOCKED
                                else -> SlotStatus.AVAILABLE
                            }
                            TimeSlot(label, status)
                        }
                    )
                ),
                selectedCourtName = "Sân 1",
                selectedStartTime = "15:30",
                selectedEndTime = "18:30",
                durationMinutes = 180,
                selectedSlotCount = 3,
                estimatedPrice = "450.000đ"
            ),
            onBackClick = {},
            onNextClick = {}
        )
    }
}
